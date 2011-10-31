/**
 * UITextList class
 * 
 * @since 1.2.4
 */

(function() {
	var window_ = this,
			usersConnection = [],
			userConnectionSearch = null,
			offset = 0,
			limit = 0;
	
	Util = exo.social.Util;
  Configuration = exo.social.Configuration;
  SocialUtil = eXo.social.SocialUtil;
  UISearch = exo.social.UISearch;
  UISetting = exo.social.UISetting;
  
  /**
   * UI component.
   */
  var uiComponent = {
  	UILoading: '#UILoading',
  	GadgetUIIconList: '#GadgetUIIconList',
  	GadgetConnectionSetting: '#GadgetConnectionSetting',
  	GadgetMemberMore: '#GadgetMemberMore',
  	UITextListListContent: '#UITextListListContent',
  	UITextListBackToListAndPeopleDirectory: '#UITextListBackToListAndPeopleDirectory',
  	BackToUITextListFromSearch: '#BackToUITextListFromSearch',
  	UITextListPeopleDirectory: '#UITextListPeopleDirectory',
  	UITextListLoadMore: '#UITextListLoadMore',
  	UITextListMoreContent: '#UITextListMoreContent'
  };
  
  /**
   * The UI Mode.
   */
  var isSearchMode = false;
  
  /**
   * The constructor.
   */
  function UITextList() {
  	
  }
  
  /**
   * Set the search mode.
   * 
   * @param mode
   */
  UITextList.setSearchMode = function(mode) {
  	isSearchMode = mode;
  };
  
  /**
   * Get the search mode.
   * 
   * @return
   */
  UITextList.getSearchMode = function() {
  	return isSearchMode;
  };
  
  /**
   * Get the user connection.
   * 
   * @return
   */
  UITextList.getUserConnection = function() {
  	return usersConnection;
  };
  
  /**
   * Set the user connection.
   * 
   * @param list
   */
  UITextList.setUserConnection = function(list) {
  	usersConnection = list;
  };
  
  /**
   * Get the user connection search.
   * 
   * @return
   */
  UITextList.getUserConnectionSearch = function() {
  	return userConnectionSearch;
  };
  
  /**
   * Set the user connection search.
   * 
   * @param list
   */
  UITextList.setUserConnectionSearch = function(list) {
  	userConnectionSearch = list;
  };
  
  /**
   * Set the offset.
   * 
   * @param off
   */
  UITextList.setOffset = function(off) {
  	offset = off;
  };
  
  /**
   * Get the offset.
   * 
   * @return
   */
  UITextList.getOffset = function() {
  	return offset;
  };
  
  /**
   * Set the limit.
   * 
   * @param lim
   */
  UITextList.setLimit = function(lim) {
  	limit = lim;
  };
  
  /**
   * Get the limit.
   * 
   * @return
   */
  UITextList.getLimit = function() {
  	return limit;
  };
  
  /**
   * Get user text list block.
   * 
   * @param userConnectionList
   * @return
   */
  function getUserTextListBlock(userConnectionList) {
  	var userBlock = [];
  	$.each(userConnectionList, function(index, value) {
  		userBlock.push('<li><a href="#" class="Icon"> ' + value.displayName + ' </a><span>' + value.activityTitle + '</span></li>'); 
  	});
  	return userBlock.join('');
  }
  
  /**
   * Display the user connection activities.
   * 
   * @param userConnectionList
   */
  function display(userConnectionList) {
  	debug.info("userConnectionList in UITextList:");
  	debug.debug(userConnectionList);
  	
  	debug.info('UITextList.getUserConnectionSearch()');
  	debug.debug(UITextList.getUserConnectionSearch());
  	
  	if (UITextList.getUserConnectionSearch() !== null) {
  		$(uiComponent.UITextListListContent).empty();
  	}
  	
  	$("#GadgetUITextList").append('<ul class="ListContent" id="UITextListListContent"></ul>');
  	if (userConnectionList === null || userConnectionList.length === 0) {
  		$(uiComponent.UITextListListContent).append("No latest updated activities of connections");
  		if ($(uiComponent.UITextListMoreContent).length > 0) {
  			$(uiComponent.UITextListMoreContent).hide();
  		}
  		if (UITextList.getSearchMode() === true) {
  			var addBlock = '<div id="UITextListBackToListAndPeopleDirectory">' + 
		 											'<a href="#" class="Link" id="BackToUITextListFromSearch">Back to List</a> | <a href="#" class="Link">People Directory</a>'  + 
		 										'</div>';
  			if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UITextListMoreContent).after(addBlock);
  			}
  			$(uiComponent.UITextListPeopleDirectory).hide();
  		}
  	} else {
  		$(uiComponent.UITextListListContent).append(getUserTextListBlock(userConnectionList));
  		if ($(uiComponent.UITextListMoreContent).length === 0) {
  			$(uiComponent.UITextListListContent).after('<div class="MoreContent" id="UITextListMoreContent"><a href="#" class="ReadMore" id="UITextListLoadMore"> Load more ... </a></div>');
  		}
  		//search mode
  		if (UITextList.getUserConnectionSearch() !== null) {
  			var addBlock = '<div id="UITextListBackToListAndPeopleDirectory">' + 
  										 		'<a href="#" class="Link" id="BackToUITextListFromSearch">Back to List</a> | <a href="#" class="Link">People Directory</a>'  + 
  										 	'</div>';
  			
  			if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
  				$(uiComponent.UITextListMoreContent).after(addBlock);
  			}
  			$(uiComponent.UITextListPeopleDirectory).hide();
  		}
  		
  		var isLoadMore = userConnectionList.length % UITextList.getLimit();
    	if (userConnectionList !== null && userConnectionList.length > 0 && isLoadMore === 0) {
    		$(uiComponent.UITextListMoreContent).show();
    		if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
    			UITextList.setOffset(UITextList.getOffset() + UITextList.getLimit());
    		} else {
    			UISearch.setOffset(UISearch.getOffset() + UISearch.getLimit());
    		}
    	} else {
    		if ($(uiComponent.UITextListMoreContent).length > 0) {
    			$(uiComponent.UITextListMoreContent).hide();
    		}
    	}
  	}
  	gadgets.window.adjustHeight();
  }
  
  $(uiComponent.BackToUITextListFromSearch).live("click", function() {
		UITextList.setUserConnectionSearch(null);
		UISearch.setNameToSearch(null);
		$(uiComponent.UITextListListContent).empty();
		$(uiComponent.UITextListBackToListAndPeopleDirectory).remove();
		$(uiComponent.UITextListPeopleDirectory).show();
		
		var isMore = UITextList.getUserConnection().length % UITextList.getLimit();
		if (isMore === 0) {
			UITextList.setOffset(UITextList.getOffset() - UITextList.getLimit());
		}
		
		UITextList.display(UITextList.getUserConnection());
		$("#SearchTextBox").val('Quick Search');
	});
  
	$(uiComponent.UITextListLoadMore).live("click", function() {
		
		debug.info("$(uiComponent.UITextListLoadMore).length: ");
		debug.debug($(uiComponent.UITextListLoadMore).length);
		
		if ($(uiComponent.UITextListBackToListAndPeopleDirectory).length === 0) {
			UITextList.loadMore();
		} else {
			UISearch.loadMore();
		}
	});
  
  /**
   * Displays the user connection activities.
   */
  UITextList.display = function() {
  	UISearch.initSearchInput({offset: 0, limit: 10, viewType: "TEXT_LIST"});
  	
  	$(uiComponent.UILoading).hide();
  	$(uiComponent.GadgetMemberMore).hide();
  	$(uiComponent.GadgetUIIconList).hide();
  	
  	if (UITextList.getUserConnectionSearch() === null) {
  		display(UITextList.getUserConnection());
  	} else {
  		display(UITextList.getUserConnectionSearch());
  	}
  };

  /**
   * Load more user connection activities.
   */
  UITextList.loadMore = function() {
  	var peopleRestUrl = Configuration.portalEnvironment.peopleRestUrl +
  											"?offset=" + UITextList.getOffset() + 
												"&limit=" + UITextList.getLimit();
  	
  	debug.info("peopleRestUrl in UITextList:");
  	debug.debug(peopleRestUrl);
  	
  	//Get user connection activities when click load more.
  	Util.makeRequest(peopleRestUrl, function(response) {
			var userConnectionsActivities = Util.parseUserConnectionActivities(response);
			
			if (userConnectionsActivities !== null && userConnectionsActivities.length > 0) {
				$.merge(usersConnection, userConnectionsActivities);
			}
			
			display(userConnectionsActivities);
		});
  };
  
  window_.exo = window_.exo || {};
  window_.exo.social = window_.exo.social || {};
  window_.exo.social.UITextList = UITextList;
})();