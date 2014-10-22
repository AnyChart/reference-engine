/** @jsx React.DOM */
goog.provide('app.view');
goog.require('goog.dom');
goog.require('goog.style');

var SearchField = React.createClass({displayName: 'SearchField',

    getInitialState: function() {
    },

    render: function() {
    }
});

var TreeNode = React.createClass({displayName: 'TreeNode',

    getInitialState: function() {
	return { visible: false };
    },

    getTitle: function() {
	var name = this.props.node["name"];
	switch (this.props.node.kind) {
	    case "namespace": return this.props.node["full-name"];
	    case "enum": return "[" + name +"]";
	    case "typedef": return "{" + name +"}";
	    case "function": return name +"()";
	    default: return name;
	}
    },

    getLink: function() {

	var kind = this.props.node.kind;
	var isTopLevel = goog.array.contains(["namespace", "class", "typedef", "enum"], kind);
	var name = this.props.node["full-name"];
	if (!isTopLevel && name.indexOf("#") == -1) {
	    var index = name.lastIndexOf(".");
	    name = name.substr(0, index) + "#" + name.substr(index + 1);
	}
	
	if (app.project != null)
	    return "/" + app.project + "/" + app.version + "/" + name;
	return "/" + name;
    },

    toggleTree: function(e) {
	this.setState({visible: !this.state.visible});
	return app.loadPage(this.getLink(), e);
    },
    
    render: function() {
	var node = this.props.node;

	var icon = null;
	if (node.kind == "class" || node.kind == "namespace")
	    if (this.state.visible) {
		icon = React.DOM.i({className: "fa fa-chevron-down"});
	    }else {
		icon = React.DOM.i({className: "fa fa-chevron-right"});
	    }
	

	var children = null;
	if (node.children && this.state.visible)
	    children = React.DOM.ul({ref: "list"}, 
	      goog.array.map(node.children, function(node) {
		  return TreeNode({key: node["full-name"], node: node});
	      })
	    );
	
	return React.DOM.li({key: this.props.node["full-name"]}, 
	         React.DOM.a({href: this.getLink(), onClick: this.toggleTree}, 
	         icon, this.getTitle()), 
	         children
	       );
    }
});

var TreeView = React.createClass({displayName: 'TreeView',
    render: function() {
	var self = this;
	return React.DOM.ul(null, goog.array.map(self.props.tree, function(node) {
	    return TreeNode({key: node["full-name"], node: node});
	}));
    }
});