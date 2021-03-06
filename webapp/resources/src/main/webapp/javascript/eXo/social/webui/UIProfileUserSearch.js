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

/**
 * Profile user search.
 * @class
 * @scope public
 */
function UIProfileUserSearch() {
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.nameTextObj = null;
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.posTextObj = null;
   
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.skillTextObj = null;
   
   /**
    * Contact name search text box object.
    * @scope private.
    */
   this.genderSelObj = null;
   
   /**
    * 
    */
   this.filterBlock = null;
};

/**
 * When form load at the first time, init controls.
 * TODO : remove. autosuggest must be implemenented by an ajax call! not by pushing all names in the client!!
 */
UIProfileUserSearch.prototype.onLoad = function(uicomponentId) {
	var DOMUtil = eXo.core.DOMUtil;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	var posEl = DOMUtil.findDescendantById(profileSearch, 'position');
	var skillEl = DOMUtil.findDescendantById(profileSearch, 'skills');
	var filterBlock = DOMUtil.findDescendantById(profileSearch, 'Filter');
	var genderEl = DOMUtil.findDescendantsByTagName(profileSearch, 'select');
	var moreSearchEl = DOMUtil.findDescendantById(profileSearch, 'MoreSearch');
	var hideMoreSearchEl = DOMUtil.findDescendantById(profileSearch,'HideMoreSearch');
	// Get default value
	var defaultUserContact = document.getElementById('defaultUserContact').value;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultSkills = document.getElementById('defaultSkills').value;
	var defaultGender = document.getElementById('defaultGender').value;
	// Default value set in component
	var defaultNameVal = "name";
	var defaultPosVal = "position";
	var defaultSkillsVal = "skills";
	var defaultGenderVal = "Gender";
	// filter has input value or not
	var hasFilter = false;
	
	this.nameTextObj = searchEl;
	this.posTextObj = posEl;
	this.skillTextObj = skillEl;
	this.genderSelObj = genderEl;
	this.filterBlock = filterBlock;
	
	if ((posEl.value != defaultPos) || (skillEl.value != defaultSkills) || (genderEl[0].value != defaultGender)) {
		hasFilter = true;
	}
	
	// Need for the first run time or component is empty then initialize the component value.
	if ((searchEl.value == defaultNameVal) || (searchEl.value.trim().length == 0)) searchEl.value = defaultUserContact;
	if ((posEl.value == defaultPosVal) || (posEl.value.trim().length == 0)) posEl.value = defaultPos;
	if ((skillEl.value == defaultSkillsVal) || (skillEl.value.trim().length == 0)) skillEl.value = defaultSkills;
	
	(searchEl.value != defaultUserContact) ? (searchEl.style.color = '#000000') : (searchEl.style.color = '#C7C7C7');
	(posEl.value != defaultPos) ? (posEl.style.color = '#000000') : (posEl.style.color = '#C7C7C7');
	(skillEl.value != defaultSkills) ? (skillEl.style.color = '#000000') : (skillEl.style.color = '#C7C7C7');
	
	if (hasFilter) {
		moreSearchEl.style.display='none';
		hideMoreSearchEl.style.display='block';
		filterBlock.style.display='block';
	} else {
		posEl.value = defaultPos;
		skillEl.value = defaultSkills;
		genderEl[0].value= defaultGender;
		moreSearchEl.style.display='block';
		hideMoreSearchEl.style.display='none';
		filterBlock.style.display='none';
	}
	
	this.initTextBox();
};

/**
 * Set init value and event for control.
 * @scope private.
 */
UIProfileUserSearch.prototype.initTextBox = function() {
	var nameEl = this.nameTextObj;
	var posEl = this.posTextObj;
	var skillEl = this.skillTextObj;
	var genderEl = this.genderSelObj;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchId = 'Search';
	var positionId = 'position';
	var skillsId = 'skills';
	var genderId = 'gender';
	var filterId = 'Filter';
	var defaultUserContact = document.getElementById('defaultUserContact').value;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultSkills = document.getElementById('defaultSkills').value;
	var defaultGender = document.getElementById('defaultGender').value;
	var uiProfileUserSearchObj = eXo.social.webui.UIProfileUserSearch;
	var suggestControlObj = eXo.social.webui.UIAutoSuggestControl;
	
	// Turn off auto-complete attribute of text-box control
	nameEl.setAttribute('autocomplete','off');
	posEl.setAttribute('autocomplete','off');
	skillEl.setAttribute('autocomplete','off');
	
	// Add focus event for control
	nameEl.onfocus = posEl.onfocus = skillEl.onfocus = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		var defaultValue = '';
		if (filter != null) {
			filter.style.color="#000000";
			if (elementId == searchId) { 
				defaultValue = defaultUserContact;
			} else if (elementId == positionId) {
				defaultValue = defaultPos;
			} else {
				defaultValue = defaultSkills;
			}
			if (filter.value == defaultValue) {
				filter.value='';
			}
		}
	}

	// Add blur event for control
	nameEl.onblur = posEl.onblur = skillEl.onblur = function(event) {
		var e = event || window.event;
		var filter = e.srcElement || e.target;
		var elementId = filter.id;
		
		if (filter != null) {
			
			if (elementId == searchId) { 
				defaultValue = defaultUserContact;
				// If current text-box is contact name apply suggestion 
				suggestControlObj.hideSuggestions();
			} else if (elementId == positionId) {
				defaultValue = defaultPos;
			} else {
				defaultValue = defaultSkills;
			}
			
			if (filter.value.trim() == '') {
				filter.style.color="#C7C7C7";
				filter.value=defaultValue;
			}
			
			if (filter.value.trim() == defaultGender) filter.style.color="#C7C7C7";
		}
	}
	
	// Add keydown event for control
	nameEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(textBox, 'UIForm');
		if(keynum == 13) {
			posEl.value = defaultPos;
			skillEl.value = defaultSkills;
			genderEl[0].value= defaultGender;
			
			suggestControlObj.hideSuggestions();
			uiProfileUserSearchObj.submitSearchForm(textBox);
		} else if (textBox.id == searchId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}

	
	// Add keydown event for control
	posEl.onkeydown = skillEl.onkeydown = genderEl.onkeydown = function(event) {
		var e = event || window.event;
		var textBox = e.srcElement || e.target;
		var keynum = e.keyCode || e.which;  
		var searchForm = DOMUtil.findAncestorByClass(textBox, 'UIForm');
		
		if(keynum == 13) {
			suggestControlObj.hideSuggestions();
			uiProfileUserSearchObj.submitSearchForm(textBox);
		} else if (textBox.id == searchId) {
			// Other keys (up and down key)
			suggestControlObj.handleKeyDown(e);
		} else {
			// ignore
		}
	}
	
	// Add suggestion capability for user contact name search text-box.
	suggestControlObj.load(nameEl, uiProfileUserSearchObj);
}

/**
 * Set default values for filter block component if it not display.<br>
 */
UIProfileUserSearch.prototype.clickSearch = function(uicomponentId) {
	var posEl = this.posTextObj;
	var skillEl = this.skillTextObj;
	var genderEl = this.genderSelObj;
	var filterBlock = this.filterBlock;
	var defaultPos = document.getElementById('defaultPos').value;
	var defaultSkills = document.getElementById('defaultSkills').value;
	var defaultGender = document.getElementById('defaultGender').value;
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var profileSearch = document.getElementById(uicomponentId);
	var searchEl = DOMUtil.findDescendantById(profileSearch, 'Search');
	
	var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');

	if (filterBlock.style.display == 'none') {
		posEl.value = defaultPos;
		skillEl.value = defaultSkills;
		genderEl[0].value= defaultGender;
	}
	
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/**
 * Submit the search form.
 * @scope private.
 */
UIProfileUserSearch.prototype.submitSearchForm = function(searchEl /*input text box*/) {
	var DOMUtil = eXo.core.DOMUtil;
	var UIForm = eXo.webui.UIForm;
	var searchForm = DOMUtil.findAncestorByClass(searchEl, 'UIForm');
	if (searchForm != null ) UIForm.submitForm(searchForm.id, 'Search', true);
}

/**
 *	Display or not for advance search block.
 *	
 *	@var newLabel {String} Label to be change when it is displayed.
 *       filterId {String} Id of filter block.
 *       elementId {String} Id of element that is displayed
 *       currentEl {Object} Element is clicked.
 *	@return void					
 *  @scope private.	
 */
UIProfileUserSearch.prototype.toggleFilter = function(newLabel, filterBlockId, elementId, currentEl) {
	var filter = document.getElementById(filterBlockId);
	
	var element = document.getElementById(elementId);
	
	if (filter.style.display == 'none') { // Click More
		currentEl.style.display = 'none';
		element.innerHTML = newLabel;
		element.style.display = 'block';
		filter.style.display = 'block';
    } else { // Click Hide
    	currentEl.style.display = 'none';
    	element.innerHTML = newLabel;
    	element.style.display = 'block';
    	filter.style.display = 'none';
    }
};

/*===================================================================*/
if(!eXo.social) eXo.social = {};
if(!eXo.social.webui) eXo.social.webui = {};
eXo.social.webui.UIProfileUserSearch = new UIProfileUserSearch();
