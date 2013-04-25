function prepareElementForEdit(target) {
	// Replaces all free range text that's not wrapped in pre, span, p into a span element
	// only once if prepared.
	return;
	// Turning off code
	if (target.nowickPrepared) {
		return;
	}
	
	var textElements = $(target).contents();
	
	for(var i =0; i < textElements.length; ++i) {
		var element = textElements[i];
		if (element.nodeType == 3) {
			var text = element.nodeValue.trim();
			if (text.length ==0) {
				$(element).remove();
			}

//			var parent = element.parentElement;
//			if (parent.nodeName != 'PRE' || parent.nodeName != 'SPAN' || parent.nodeName != 'P' ) {
//				var text = element.nodeValue.trim();
//				if (text.length ==0) {
//					continue;
//				}
//				
//				var previousSibling = element.previousSibling;
//				
//				var spanElement = document.createElement("span");
//				$(spanElement).text(text);
//				$(element).remove();
//				if (previousSibling) {
//					$(previousSibling).after(spanElement);
//				}
//				else {
//					$(parent).prepend(spanElement);
//				}
//			}
		}
		else {
			prepareElementForEdit(element);
		}
	}
	
	target.nowickPrepared = true;
}

function handleKeydown(evt) {
	var keyCode = evt.keyCode;
	var ctrl = evt.ctrlKey;
	var alt = evt.altKey;
	var shift = evt.shiftKey;
	
	var selection = document.getSelection();
	if (keyCode == 13) { // Handle Enter
		
	}
	else if (keyCode == 9) { // Handle Tab
		handleTab(evt);
	}
	else if (keyCode == 66 && ctrl) { // Ctrl-B Handle Bold
		
	}
	else if (keyCode == 73 && ctrl) { // Ctrl-I Handle Italics
		
	}
	else if (keyCode == 85 && ctrl) { // Ctrl-U Handle Underline
		
	}
	else {
		return true;
	}

	return false;
}

function handleTab(evt) {
	var selection = document.getSelection();
	
	var node = selection.anchorNode;
	var parentElement = node.parentElement;
	
	var range = selection.getRangeAt(0);
	var startRange = range.startOffset;
	var endRange = range.endOffset;
	var commonAncestor = range.commonAncestorContainer;
	
	var shift = evt.shiftKey;

	// Tabbing if in Li and if first element
	var liElement = node.parentElement;
	if (liElement.nodeName == "LI") {
		if (node.previousSibling == null && startRange == 0) {
			var finalElement = liElement;
			var ulParent = liElement.parentElement;

			// If it's the first item in the list, we want to indent
			if ($(ulParent).children()[0] == liElement) {
				var tabmargin = ulParent.tabmargin ? ulParent.tabmargin : 0;
				
				if (shift) {
					if (tabmargin <= 0) {
						// No more tabs to work with. Now we remove this element from the ul.
						// Wrap in <div> if contents are text.
						var grandParent = ulParent.parentElement;
						if (grandParent.nodeName == "LI") {
							// if the grand parent of the current Li element is an li, 
							// we'll just move this as a sibling of the grandparent.
							$(grandParent).after(liElement);
						}
						else {
							var div = document.createElement("div");
							$(liElement).contents().each(function(item) {
								$(div).append(this);
							});
							$(ulParent).before(div);
							finalElement = div;
							$(liElement).remove();
						}
					}
					else {
						tabmargin -= 16;
						ulParent.tabmargin = tabmargin;
					}
				} else {
					tabmargin += 16;
					ulParent.tabmargin = tabmargin;
				}
				
				$(ulParent).css("margin-left", tabmargin);
			}
			else {
				if (shift) {
					var prev = $(liElement).prev();
					var next = $(liElement).next();
					var grandParent = ulParent.parentElement;
					var after;
					if (grandParent.nodeName == "LI") {
						// if the grand parent of the current Li element is an li, 
						// we'll just move this as a sibling of the grandparent.

						parentUL = liElement;
						var grandParent = ulParent.parentElement;
						$(grandParent).after(liElement);
						
						if (next.length) {
							var parentUL = document.createElement("ul");

							while(next.length) {
								$(parentUL).append(next);
								next = $(next).next();
							}
							
							$(liElement).append(parentUL);
						}
					}
					else {
						var elem;
						if ($(liElement).contents().length != 1 || $(liElement).contents()[0].nodeType == 3) {
							elem = document.createElement("div");
							$(liElement).contents().each(function(item) {
								$(elem).append(this);
							});
						}
						else {
							elem = $(liElement).contents()[0];
						}

						finalElement = elem;
						$(ulParent).after(elem);
						
						$(liElement).remove();
						if (next.length) {
							var parentUL = document.createElement("ul");

							while(next.length) {
								$(parentUL).append(next);
								next = $(next).next();
							}
							
							$(elem).after(parentUL);
						}
					}
				}
				else {
					// Check to see if previous list item has a ul. If so, add to that ul.
					var prevListItem = $(liElement).prev();
					if (prevListItem[0].nodeName=="LI") {
						var prevChildren = prevListItem.children();
						var lastItem = prevChildren[prevChildren.length -1];
						
						// If a previous UL exists, we'll add the LI to this ul
						if (lastItem && lastItem.nodeName=="UL") {
							$(liElement).appendTo(lastItem);
						}
						else {
							// If it doesn't, we'll create a ul and add it.
							var ul = document.createElement("ul");
							$(prevListItem).append(ul);
							$(liElement).appendTo(ul);
						}
					}
					else {
						// Otherwise we'll just tab away.
						var tabmargin = liElement.tabmargin ? liElement.tabmargin : 0;
						
						tabmargin += 16;
						liElement.tabmargin = tabmargin;
						
						$(liElement).css("margin-left", tabmargin);
					}
				}
			}
			selection.collapse(finalElement.firstChild, 0);
			
			// Remove away the ul if it's empty.
			if($(ulParent).children().length == 0) {
				$(ulParent).remove();
			} 
		}
	}
	
	if (commonAncestor.nodeType == 3) { // Text type. Erase range and then insert a tab character
		var startRange = range.startOffset;
		var endRange = range.endOffset;
		
		if ((endRange - startRange) == 0) {
			
		}
	}
}

function findTopLevelLI(element) {
	if (element.nodeName == "LI") {
		
	}
}

function deleteTextRange(textElement, startRange, endRange) {
	var data = textElement.data;
	
}



