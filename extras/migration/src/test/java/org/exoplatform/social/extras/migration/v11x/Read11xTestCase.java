/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.extras.migration.v11x;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.extras.migraiton.MigrationConst;
import org.exoplatform.social.extras.migraiton.loading.DataLoader;
import org.exoplatform.social.extras.migraiton.reader.NodeReader;
import org.exoplatform.social.extras.migraiton.reader.NodeReader11x;
import org.exoplatform.social.extras.migration.AbstractMigrationTestCase;
import org.exoplatform.social.extras.migration.Utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Read11xTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;
  private Session session;
  private OrganizationService organizationService;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    session = Utils.getSession();
    loader = new DataLoader("migrationdata-11x.xml", session);
    loader.load();
    rootNode = session.getRootNode();

    PortalContainer container = PortalContainer.getInstance();
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

  }

  @Override
  public void tearDown() throws Exception {

    NodeIterator it = rootNode.getNode("exo:applications").getNode("Social_Identity").getNodes();

    while(it.hasNext()) {
      String userName = ((Node) it.next()).getProperty("exo:remoteId").getString();
      organizationService.getUserHandler().removeUser(userName, true);
    }

    Group spaces = organizationService.getGroupHandler().findGroupById("/spaces");
    for (Group group : (Collection<Group>) organizationService.getGroupHandler().findGroups(spaces)) {
      organizationService.getGroupHandler().removeGroup(group, true);
    }

    rootNode.getNode("exo:applications").remove();

    super.tearDown();

  }

  public void testReadIdentity() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readIdentities(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkIdentity(dis, "exo:identity", "user_idA", "organization");
    checkIdentity(dis, "exo:identity[2]", "user_idB", "organization");
    checkIdentity(dis, "exo:identity[3]", "user_idC", "organization");
    checkIdentity(dis, "exo:identity[4]", "user_idD", "organization");
    checkIdentity(dis, "exo:identity[5]", "user_idE", "organization");

    checkIdentity(dis, "user_a", "user_a", "organization");
    checkIdentity(dis, "user_b", "user_b", "organization");
    checkIdentity(dis, "user_c", "user_c", "organization");
    checkIdentity(dis, "user_d", "user_d", "organization");
    checkIdentity(dis, "user_e", "user_e", "organization");

    String uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[2]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[3]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

  }

  public void testReadRelationship() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readRelationships(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkRelationship(dis, "exo:relationship", "exo:identity[3]", "exo:identity[2]", "CONFIRM");
    checkRelationship(dis, "exo:relationship[2]", "exo:identity[3]", "exo:identity[4]", "PENDING");
    checkRelationship(dis, "exo:relationship[3]", "user_a", "exo:identity[4]", "CONFIRM");
    checkRelationship(dis, "exo:relationship[4]", "user_d", "exo:identity", "CONFIRM");
    checkRelationship(dis, "exo:relationship[5]", "user_b", "user_a", "PENDING");
    checkRelationship(dis, "exo:relationship[6]", "user_c", "user_d", "CONFIRM");
    checkRelationship(dis, "ec1bbdea2e8902a901cf62bd95f0bdc8", "user_c", "user_a", "CONFIRM");

  }

  public void testReadSpaces() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readSpaces(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkSpace(dis, "exo:space", "a", new String[]{"user_a","user_b","user_d"}, null);
    checkSpace(dis, "exo:space[2]", "b", null, null);
    checkSpace(dis, "exo:space[3]", "c", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[4]", "d", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[5]", "e", new String[]{"user_c"}, new String[]{"user_a","user_d"});

  }

  public void testReadActivity() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readActivities(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkActivity(dis, "ad25a8622e8902a9004557b913a2982b", "organization", "user_a", "user_a", new String[]{"SENDER=user_b", "RECEIVER=user_a"}, "@user_b has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872377", null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982c", "organization", "user_a", "user_a", new String[]{"SENDER=user_c", "RECEIVER=user_a"}, "@user_c has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872378", null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982e", "organization", "user_a", "user_a", null, "foo", null, "exosocial:relationship", "1298642872380", "IS_COMMENT");

    String replyToId = rootNode.getNode("exo:applications/Social_Activity/organization/user_a/published/ad25a8622e8902a9004557b913a2982e").getUUID();
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982d", "organization", "user_a", "user_a", new String[]{"SENDER=user_d", "RECEIVER=user_a"}, "@user_d has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872379", replyToId);

    String spaceId = rootNode.getNode("exo:applications/Social_Space/Space/exo:space").getUUID();
    checkActivity(dis, "ad25a8622e8902a9004557b913a2983b", "space", spaceId, "user_a", null, "@user_a has joined.", null, "exosocial:spaces", "1298642872387", null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2983c", "space", spaceId, "user_b", null, "@user_b has joined.", null, "exosocial:spaces", "1298642872388", null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2983d", "space", spaceId, "user_c", null, "@user_c has joined.", null, "exosocial:spaces", "1298642872389", null);

  }

  private void checkIdentity(DataInputStream dis, String nodeName, String remoteId, String providerId) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Identity/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:identity", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:providerId", dis.readUTF());
    assertEquals(providerId, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:remoteId", dis.readUTF());
    assertEquals(remoteId, dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

  private void checkRelationship(DataInputStream dis, String nodeName, String identitiy1, String identitiy2, String status) throws IOException, RepositoryException {

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Relationship/" + nodeName, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:relationship", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity1Id", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + identitiy1).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity2Id", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + identitiy2).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:status", dis.readUTF());
    assertEquals(status, dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

  private void checkSpace(DataInputStream dis, String nodeName, String suffix, String[] pendingUsers, String[] invitedUsers) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Space/Space/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:space", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:description", dis.readUTF());
    assertEquals("foo " + suffix, dis.readUTF());
    
    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:groupId", dis.readUTF());
    assertEquals("/spaces/name" + suffix, dis.readUTF());

    if (invitedUsers != null) {
      assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
      assertEquals(invitedUsers.length, dis.readInt());
      assertEquals("exo:invitedUsers", dis.readUTF());
      for (String invitedUser : invitedUsers) {
        assertEquals(invitedUser, dis.readUTF());
      }
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:name", dis.readUTF());
    assertEquals("Name " + suffix, dis.readUTF());

    if (pendingUsers != null) {
      assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
      assertEquals(pendingUsers.length, dis.readInt());
      assertEquals("exo:pendingUsers", dis.readUTF());
      for (String pendingUser : pendingUsers) {
        assertEquals(pendingUser, dis.readUTF());
      }
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:priority", dis.readUTF());
    assertEquals("2", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:registration", dis.readUTF());
    assertEquals("validation", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:type", dis.readUTF());
    assertEquals("classic", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:url", dis.readUTF());
    assertEquals("name" + suffix, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:visibility", dis.readUTF());
    assertEquals("private", dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

  private void checkActivity(DataInputStream dis, String nodeName, String provider, String owner, String poster, String[] params, String title, String titleTemplate, String type, String timestamp, String replyToId) throws IOException, RepositoryException {

    String path;
    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Activity/" + provider + "/" + owner + "/published/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:activity", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:hidden", dis.readUTF());
    assertEquals("false", dis.readUTF());

    if (params != null) {
      assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
      assertEquals(params.length, dis.readInt());
      assertEquals("exo:params", dis.readUTF());
      for (String param : params) {
        assertEquals(param, dis.readUTF());
      }
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:postedTime", dis.readUTF());
    assertEquals(timestamp, dis.readUTF());

    if (replyToId != null) {
      assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:replyToId", dis.readUTF());
      assertEquals(replyToId, dis.readUTF());
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:title", dis.readUTF());
    assertEquals(title, dis.readUTF());

    if (titleTemplate != null) {
      assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:titleTemplate", dis.readUTF());
      assertEquals(titleTemplate, dis.readUTF());
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:type", dis.readUTF());
    assertEquals(type, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:updatedTimestamp", dis.readUTF());
    assertEquals(timestamp, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:userId", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + poster).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

}
