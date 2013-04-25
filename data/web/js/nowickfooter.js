$.namespace('nowick');

var floatingEditor;
nowick.FloatingEditor = Backbone.View.extend({
	events: {
		"click #nowickeditorclose": "stopEdit"
	},
	initialize: function(settings) {
		$(this.el).draggable({cursor: 'move'});
		$(this.el).hide();
	},
	render: function() {
		
	},
	turnOnEditMode: function(evt) {
		$(".editable").addClass("nowick-editable");
		$(".editable").bind("click", this.editableClicked);
	},
	turnOffEditMode: function(evt) {
		this.stopEdit(evt);
		$(".editable").removeClass("nowick-editable");
		$(".editable").unbind("click", this.editableClicked);
	},
	editableClicked: function(evt) {
		console.log("edit clicked");
		floatingEditor.startEdit(evt.currentTarget);
		$(evt.currentTarget).unbind("click", this.editableClicked);
	},
	stopEdit: function(evt) {
		if (this.editingTarget) {
			$(this.editingTarget).removeClass('editing');
			$(this.editingTarget).click(this.editableClicked);
			$(this.editingTarget).unbind("keydown", handleKeydown)
			this.editingTarget.contentEditable = false;
		}
		$(this.el).fadeOut(100);
	},
	startEdit: function(target) {
		console.log("start edit called");
		
		if (this.editingTarget) {
			$(this.editingTarget).removeClass('editing');
			$(this.editingTarget).click(this.editableClicked);
			$(this.editingTarget).unbind("keydown", handleKeydown);
			this.editingTarget.contentEditable = false;
		}
		
		this.editingTarget = target;
		$(target).unbind("click", this.editableClicked);
		$(target).bind("keydown", handleKeydown);
		prepareElementForEdit(target);
		
		var currentTarget = target;
		$(currentTarget).addClass("editing");
		currentTarget.contentEditable = true;
		
		var bounding = target.getBoundingClientRect();
		$(this.el).css("left", bounding.left);
		var floatingBarHeight = $(this.el).height();
		
		var moveTop = 0;
		if (bounding.top < $(this.el).height()) {
			moveTop = bounding.bottom + 3;
		}
		else {
			moveTop = bounding.top - floatingBarHeight - 3;
		}
	
		if ($(this.el).is(":visible")) {
			$(this.el).animate({left: bounding.left, top: moveTop}, 100);
		}
		else {
			$(this.el).css('left', bounding.left);
			$(this.el).css('top', moveTop);
			$(this.el).fadeIn('fast');
		}
		var toolbar = this.el;
	}
});


var footerView;
nowick.FooterView= Backbone.View.extend({
	events : {
		"click #nowickfooter-expandclose" : "animateToggle",
		"click #nowick-edit-button" : "editPage",
		"click #nowick-cancel-button" : "cancelEditPage"
	},
	initialize : function(settings) {
		$("#nowickfooter-menu").hide();
		$("#nowick-revert-button").attr("disabled", "disabled");
		$("#nowick-cancel-button").attr("disabled", "disabled");
		$("#nowick-save-button").attr("disabled", "disabled");
	},
	render: function() {
	},
	animateToggle: function(evt) {
		if ($("#nowickfooter-menu").is(":visible")) {
			$("#nowickfooter-menu").slideUp('fast');
			$("#nowickfooter-expandclose").removeClass('close');
		}
		else {
			$("#nowickfooter-menu").slideDown('fast');
			$("#nowickfooter-expandclose").addClass('close');
		}
	},
	editPage: function(evt) {
		$("#nowick-revert-button").removeAttr("disabled");
		$("#nowick-cancel-button").removeAttr("disabled");
		$("#nowick-save-button").removeAttr("disabled");
		$("#nowick-edit-button").attr("disabled", "disabled");

		floatingEditor.turnOnEditMode();
	},
	cancelEditPage: function(evt) {
		$("#nowick-edit-button").removeAttr("disabled");
		$("#nowick-revert-button").attr("disabled", "disabled");
		$("#nowick-cancel-button").attr("disabled", "disabled");
		$("#nowick-save-button").attr("disabled", "disabled");
		//Aloha.jQuery(".editable").mahalo();
		$(".editable").removeClass("nowick-editable");
		$(".editable").unbind("click", this.editableClicked);
	
		floatingEditor.turnOffEditMode();
	}
});

function createElement(data) {
	var element = document.createElement(data.type);
	
	if (data.id) {
		$(element).attr("id", data.id);
	}
	
	if (data.text) {
		$(element).text(data.text);
	}
	
	if (data.classes) {
		if (data.classes instanceof Array){
			for (var i = 0; i < data.classes.length; ++i) {
				$(element).addClass(data.classes[i]);
			}
		}
		else {
			$(element).addClass(data.classes);
		}
	}
	
	if (data.parent) {
		$(data.parent).append(element);
	}
	
	return element;
}

function createNowickFooter() {
	var nowickfooter = createElement({type: "div", id: "nowickfooter", parent: $("body")});
	var nowickfooterexpandclose = createElement({type: "div", id: "nowickfooter-expandclose", parent: nowickfooter});
	var nowickfootermenu = createElement({type: "div", id: "nowickfooter-menu", parent: nowickfooter});
	
	var newPageButton = createElement({type: "button", id: "nowick-newpage-button", text: "New Page", parent: nowickfootermenu});
	var editButton = createElement({type: "button", id: "nowick-edit-button", text: "Edit", parent: nowickfootermenu});
	var cancelChangesButton = createElement({type: "button", id: "nowick-cancel-button", text: "Cancel", parent: nowickfootermenu});
	
	var revertChangesButton = createElement({type: "button", id: "nowick-revert-button", text: "Revert", parent: nowickfootermenu});
	var saveChangesButton = createElement({type: "button", id: "nowick-save-button", text: "Save", parent: nowickfootermenu});
}

function createNowickEditor() {
	var nowickEditor = createElement({type: "div", id: "nowickeditor", parent: $("body")});
	//$(nowickEditor).hide();
	var nowickEditorHeader = createElement({type: "div", id: "nowickeditor-header", parent: nowickEditor});
	var nowickEditTab = createElement({type: "div", id: "nowickedittab", text: "Edit", parent: nowickEditorHeader, classes: "tab first selected"})
	var nowickLayoutTab = createElement({type: "div", id: "nowicklayouttab", text: "Layout", parent: nowickEditorHeader, classes: "tab"});
	var nowickClose = createElement({type: "div", id: "nowickeditorclose", text: "x", parent: nowickEditorHeader});
	
	var nowickEditorPane = createElement({type: "div", id: "nowickeditor-pane", parent: nowickEditor});
}

$(function() {
	createNowickFooter();
	createNowickEditor();
	footerView = new nowick.FooterView({el:$('#nowickfooter')});
	floatingEditor = new nowick.FloatingEditor({el:$('#nowickeditor')});
});
