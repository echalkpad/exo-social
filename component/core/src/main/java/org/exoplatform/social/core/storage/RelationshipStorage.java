/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.NodeProperties;
import org.exoplatform.social.common.jcr.NodeTypes;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException.Type;

/**
 * Class RelationshipStorage is a class that persist or get Relationship object
 * directly from JCR
 * 
 * @modifier tuan_nguyenxuan
 */
public class RelationshipStorage {
  private static final Log   LOG = ExoLogger.getLogger(RelationshipStorage.class);

  /** The identity manager. */
  private IdentityManager    identityManager;

  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager  sessionManager;

  /** The relationship service home. */
  private Node               relationshipServiceHome;

  /**
   * Instantiates a new RelationshipStorage.
   * 
   * @param dataLocation the data location
   */
  public RelationshipStorage(final SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Saves relationship.
   * 
   * @param relationship the relationship
   * @throws RelationshipStorageException
   */
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {
    if (relationship == null) {
      throw new RelationshipStorageException(Type.ILLEGAL_ARGUMENTS, new String[] { Relationship.class.getSimpleName() });
    }
    final Session session = sessionManager.getOrOpenSession();
    Node relationshipNode = null;

    try {
      final Node relationshipHomeNode = getRelationshipServiceHome(session);

      if (relationship.getId() == null) {
        relationshipNode = relationshipHomeNode.addNode(NodeTypes.EXO_RELATIONSHIP, NodeTypes.EXO_RELATIONSHIP);
        relationshipNode.addMixin(NodeTypes.MIX_REFERENCEABLE);
      } else {
        relationshipNode = session.getNodeByUUID(relationship.getId());
      }
      relationshipNode.setProperty(NodeProperties.RELATIONSHIP_SENDER, session.getNodeByUUID(relationship.getSender().getId()));
      relationshipNode.setProperty(NodeProperties.RELATIONSHIP_RECEIVER, 
                                   session.getNodeByUUID(relationship.getReceiver().getId()));
      relationshipNode.setProperty(NodeProperties.RELATIONSHIP_STATUS, relationship.getStatus().toString());
      relationshipNode.setProperty(NodeProperties.RELATIONSHIP_IS_SYMETRIC, relationship.isSymetric());

      if (relationship.getId() == null) {
        relationshipHomeNode.save();
        relationship.setId(relationshipNode.getUUID());
        LOG.debug("relationship " + relationship + " stored");
      } else {
        relationshipNode.save();
        LOG.debug("relationship " + relationship + " updated");
      }
    } catch (Exception e) {
      throw new RelationshipStorageException(Type.FAILED_TO_SAVE_RELATIONSHIP, e);
    } finally {
      sessionManager.closeSession();
    }
    return relationship;
  }

  /**
   * Removes the relationship.
   * 
   * @param relationship the relationship
   * @throws RelationshipStorageException
   */
  public void removeRelationship(Relationship relationship) throws RelationshipStorageException {
    Session session = sessionManager.openSession();
    try {
      Node relationshipNode = session.getNodeByUUID(relationship.getId());
      relationshipNode.remove();
      session.save();
    } catch (ItemNotFoundException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_DELETE_RELATIONSHIP_ITEM_NOT_FOUND, e);
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_DELETE_RELATIONSHIP, e);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the relationship.
   * 
   * @param uuid the uuid
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(String uuid) throws RelationshipStorageException {
    final Session session = sessionManager.getOrOpenSession();
    Relationship relationship = null;
    try {
      Node relationshipNode = session.getNodeByUUID(uuid);
      relationship = loadRelationship(relationshipNode);
    } catch (Exception e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, e);
    } finally {
      sessionManager.closeSession();
    }

    return relationship;
  }

  /**
   * Loads relationship from JCR
   * 
   * @param relationshipNode
   * @return relationship
   * @throws RepositoryException
   * @throws RelationshipStorageException 
   */
  private Relationship loadRelationship(final Node relationshipNode) throws RepositoryException, RelationshipStorageException {
    final Relationship relationship = new Relationship(relationshipNode.getUUID());

    Node idNode = relationshipNode.getProperty(NodeProperties.RELATIONSHIP_SENDER).getNode();
    relationship.setSender(getIdentityManager().getIdentity(idNode.getUUID()));

    idNode = relationshipNode.getProperty(NodeProperties.RELATIONSHIP_RECEIVER).getNode();
    relationship.setReceiver(getIdentityManager().getIdentity(idNode.getUUID()));

    relationship.setStatus(Relationship.Type.valueOf(relationshipNode.getProperty(NodeProperties.RELATIONSHIP_STATUS)
                                                                     .getString()));
    relationship.setSymetric(relationshipNode.getProperty(NodeProperties.RELATIONSHIP_IS_SYMETRIC).getBoolean());

    return relationship;
  }

  /**
   * Gets the relationship of 2 identities.
   * 
   * @param identity the identity1
   * @param identity the identity2
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(final Identity identity1, final Identity identity2) throws RelationshipStorageException {
    if (identity1 == null || identity1.getId() == null || identity2 == null || identity2.getId() == null) {
      throw new RelationshipStorageException(Type.ILLEGAL_ARGUMENTS, new String[] {identity1.toString(), identity2.toString() });
    }
    return getRelationship(identity1.getId(), identity2.getId());
  }

  /**
   * Gets the relationship of 2 identities.
   * 
   * @param string the identityId1
   * @param string the identityId2
   * @return the relationship of 2 identities
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(final String identityId1, final String identityId2) throws RelationshipStorageException {
    if (identityId1 == null || "".equals(identityId1) || identityId2 == null || "".equals(identityId2)) {
      throw new RelationshipStorageException(Type.ILLEGAL_ARGUMENTS, identityId1, identityId2);
    }
    final Session session = sessionManager.getOrOpenSession();
    List<Node> nodes = null;
    try {
      final QueryBuilder query = selectRelationship(session);
      query.and()
             .group()
               .group()
                 .equal(NodeProperties.RELATIONSHIP_SENDER, identityId1)
                 .and().equal(NodeProperties.RELATIONSHIP_RECEIVER, identityId2)
               .endGroup()
               .or()
               .group()
                 .equal(NodeProperties.RELATIONSHIP_SENDER, identityId2)
                 .and().equal(NodeProperties.RELATIONSHIP_RECEIVER, identityId1)
               .endGroup()
             .endGroup();

      nodes = query.exec();
      if (nodes == null || nodes.size() < 1) {
        return null;
      } else if (nodes.size() > 1) {
        throw new RelationshipStorageException(Type.MORE_THAN_ONE_RELATIONSHIP, identityId1, identityId2);
      } else {
        return loadRelationship(nodes.get(0));
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP_OF_THEM, null, e, identityId1, identityId2);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets count of connection with the identity.
   * 
   * @param identity
   * @return
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public int getConnectionsCount(Identity identity) throws RelationshipStorageException {
    Session session = sessionManager.getOrOpenSession();
    String identityId = identity.getId();

    try {
      QueryBuilder query = selectRelationship(session);
      return (int)query
             .and()
             .group()
               .equal(NodeProperties.RELATIONSHIP_SENDER, identityId)
               .or().equal(NodeProperties.RELATIONSHIP_RECEIVER, identityId)
             .endGroup()
             .and().equal(NodeProperties.RELATIONSHIP_STATUS, Relationship.Type.CONFIRMED.toString())
             .count();
      
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, null, e, identityId,
                                             Relationship.Type.CONFIRMED.toString());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets connections with the identity.
   * 
   * @param identity
   * @param offset
   * @param limit
   * @return number of connections belong to limitation of offset and limit.
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getConnections(Identity identity, int offset, int limit) throws RelationshipStorageException {
    Session session = sessionManager.getOrOpenSession();
    List<Identity> connections = new ArrayList<Identity>();
    String identityId = identity.getId();
    List<Node> nodes = null;
    try {
      QueryBuilder query = new QueryBuilder(session);
      query.select(NodeTypes.EXO_RELATIONSHIP, offset, limit)
           .like(NodeProperties.JCR_PATH, "/" + dataLocation.getSocialRelationshipHome() + "/%")
           .and()
           .group()
             .equal(NodeProperties.RELATIONSHIP_SENDER, identityId)
             .or().equal(NodeProperties.RELATIONSHIP_RECEIVER, identityId)
           .endGroup()
           .and().equal(NodeProperties.RELATIONSHIP_STATUS, Relationship.Type.CONFIRMED.toString());
      
      nodes = query.exec();
      for (Node node : nodes) {
        Relationship relationship = loadRelationship(node);
        connections.add(relationship.getPartner(identity));
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, null, e, identityId,
                                             Relationship.Type.CONFIRMED.toString());
    } finally {
      sessionManager.closeSession();
    }
    return connections;
  }
  
  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identity the identity
   * @param relationshipType
   * @param list identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getRelationships(final Identity identity, final Relationship.Type type,
                                             final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    if (identity == null || identity.getId() == null) {
      throw new RelationshipStorageException(Type.ILLEGAL_ARGUMENTS);
    }
    if (listCheckIdentity == null) {
      return getRelationships(identity.getId(), type, null);
    }
    List<String> identityIds = new ArrayList<String>();
    for (Identity checkingIdentity : listCheckIdentity) {
      identityIds.add(checkingIdentity.getId());
    }
    return getRelationships(identity.getId(), type, identityIds);
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identityId the identity id
   * @param relationshipType
   * @param list identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getRelationships(final String identityId, final Relationship.Type type,
                                             final List<String> listCheckIdentityId) throws RelationshipStorageException {
    final Session session = sessionManager.getOrOpenSession();
    final List<Relationship> results = new ArrayList<Relationship>();

    List<Node> nodes = null;
    try {
      final QueryBuilder query = selectRelationship(session);
      query.and()
             .group()
               .equal(NodeProperties.RELATIONSHIP_SENDER, identityId)
               .or().equal(NodeProperties.RELATIONSHIP_RECEIVER, identityId)
             .endGroup();

      if (type != null) {
        query.and().equal(NodeProperties.RELATIONSHIP_STATUS, type.toString());
      }

      if (listCheckIdentityId != null && listCheckIdentityId.size() > 0) {
        query.and().group();
        String firstCheckIdentityId = listCheckIdentityId.get(0);
        query.equal(NodeProperties.RELATIONSHIP_SENDER, firstCheckIdentityId)
             .or().equal(NodeProperties.RELATIONSHIP_RECEIVER, firstCheckIdentityId);
        listCheckIdentityId.remove(0);
        for (String checkIdentityId : listCheckIdentityId) {
          query.or().equal(NodeProperties.RELATIONSHIP_SENDER, checkIdentityId)
               .or().equal(NodeProperties.RELATIONSHIP_RECEIVER, checkIdentityId);
        }
        query.endGroup();
      }

      nodes = query.exec();
      for (final Node node : nodes) {
        final Relationship relationship = loadRelationship(node);
        results.add(relationship);
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, null, e, identityId, type.toString());
    } finally {
      sessionManager.closeSession();
    }
    return results;
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identity the identity
   * @param relationshipType
   * @param list identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getSenderRelationships(final Identity sender, final Relationship.Type type,
                                                   final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    if (sender == null || sender.getId() == null) {
      throw new RelationshipStorageException(Type.ILLEGAL_ARGUMENTS);
    }
    if (listCheckIdentity == null) {
      return getSenderRelationships(sender.getId(), type, null);
    }
    List<String> identityIds = new ArrayList<String>();
    for (Identity checkingIdentity : listCheckIdentity) {
      identityIds.add(checkingIdentity.getId());
    }
    return getSenderRelationships(sender.getId(), type, identityIds);
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identityId the identity id
   * @param relationshipType
   * @param list identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getSenderRelationships(final String senderId, final Relationship.Type type,
                                                   final List<String> listCheckIdentityId) throws RelationshipStorageException {
    final Session session = sessionManager.getOrOpenSession();
    final List<Relationship> results = new ArrayList<Relationship>();

    List<Node> nodes = null;
    try {
      final QueryBuilder query = selectRelationship(session);
      query.and().equal(NodeProperties.RELATIONSHIP_SENDER, senderId);

      if (type != null) {
        query.and().equal(NodeProperties.RELATIONSHIP_STATUS, type.toString());
      }

      if (listCheckIdentityId != null && listCheckIdentityId.size() > 0) {
        query.and().group();
        String firstCheckIdentityId = listCheckIdentityId.get(0);
        query.equal(NodeProperties.RELATIONSHIP_RECEIVER, firstCheckIdentityId);
        listCheckIdentityId.remove(0);
        for (String checkIdentityId : listCheckIdentityId) {
          query.or().equal(NodeProperties.RELATIONSHIP_RECEIVER, checkIdentityId);
        }
        query.endGroup();
      }

      nodes = query.exec();
      for (final Node node : nodes) {
        final Relationship relationship = loadRelationship(node);
        results.add(relationship);
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, null, e, senderId, type.toString());
    } finally {
      sessionManager.closeSession();
    }
    return results;
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identityId the identity id
   * @param relationshipType
   * @param list identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getReceiverRelationships(final Identity receiver, final Relationship.Type type,
                                                     final List<Identity> listCheckIdentity) throws RelationshipStorageException {
    List<String> identityIds = new ArrayList<String>();
    for (Identity checkingIdentity : listCheckIdentity) {
      identityIds.add(checkingIdentity.getId());
    }
    return getReceiverRelationships(receiver.getId(), type, identityIds);
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   * 
   * @param identityId the identity id
   * @param relationshipType
   * @param list identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getReceiverRelationships(final String receiverId, final Relationship.Type type,
                                                     final List<String> listCheckIdentityId) throws RelationshipStorageException {
    final Session session = sessionManager.getOrOpenSession();
    final List<Relationship> results = new ArrayList<Relationship>();

    List<Node> nodes = null;
    try {
      final QueryBuilder query = selectRelationship(session);
      query.and().equal(NodeProperties.RELATIONSHIP_RECEIVER, receiverId);

      if (type != null) {
        query.and().equal(NodeProperties.RELATIONSHIP_STATUS, type.toString());
      }

      if (listCheckIdentityId != null && listCheckIdentityId.size() > 0) {
        query.and().group();
        String firstCheckIdentityId = listCheckIdentityId.get(0);
        query.equal(NodeProperties.RELATIONSHIP_SENDER, firstCheckIdentityId);
        listCheckIdentityId.remove(0);
        for (String checkIdentityId : listCheckIdentityId) {
          query.or().equal(NodeProperties.RELATIONSHIP_SENDER, checkIdentityId);
        }
        query.endGroup();
      }

      nodes = query.exec();
      for (final Node node : nodes) {
        final Relationship relationship = loadRelationship(node);
        results.add(relationship);
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(Type.FAILED_TO_GET_RELATIONSHIP, null, e, receiverId, type.toString());
    } finally {
      sessionManager.closeSession();
    }
    return results;
  }

  /**
   * Gets QueryBuilder for select relationship
   * 
   * @param session
   * @return QueryBuilder
   */
  private QueryBuilder selectRelationship(final Session session) {
    final QueryBuilder query = new QueryBuilder(session);
    query.select(NodeTypes.EXO_RELATIONSHIP)
          .like(NodeProperties.JCR_PATH, "/" + dataLocation.getSocialRelationshipHome() + "/%");
    return query;
  }

  /**
   * Gets the relationship service home.
   * 
   * @param session the session
   * @return the relationship service home
   * @throws Exception the exception
   */
  private Node getRelationshipServiceHome(final Session session) {
    if (relationshipServiceHome == null) {
      try {
        String path = dataLocation.getSocialRelationshipHome();
        Util.createNodes(session.getRootNode(), path);
        relationshipServiceHome = session.getRootNode().getNode(path);
      } catch (RepositoryException re) {
        LOG.warn(re.getMessage(), re);
      }
    }
    return relationshipServiceHome;
  }

  /**
   * Gets identityManager
   * 
   * @return identityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
}
