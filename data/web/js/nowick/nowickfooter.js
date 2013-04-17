$.namespace('nowick');

var footerView;
nowick.FooterView= Backbone.View.extend({
	events : {
		"click #nowickfooter-expandclose" : "animateToggle"
	},
	initialize : function(settings) {
		$("#nowickfooter-menu").hide();
	},
	render: function() {
	},
	animateToggle: function(evt) {
		if ($("#nowickfooter-menu").is(":visible")) {
			$("#nowickfooter-menu").slideUp('fast');
		}
		else {
			$("#nowickfooter-menu").slideDown('fast');
		}
	}
});

$(function() {
	footerView = new nowick.FooterView({el:$('#nowickfooter')});
});
