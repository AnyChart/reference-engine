/** @jsx React.DOM */
goog.provide('app.view');
goog.require('goog.dom');

var TreeNode = React.createClass({displayName: 'TreeNode',

    getTitle: function() {
	switch (this.props.node.kind) {
	    case "namespace": return this.props.node["full-name"];
	    default: return this.props.node["name"];
	}
    },

    getLink: function() {
	return "/" + this.props.node["full-name"];
    },
    
    render: function() {
	var node = this.props.node;
	
	return React.DOM.li({key: this.props.node["full-name"]}, 
	         React.DOM.a({href: this.getLink()}, 
	         React.DOM.i({className: "fa fa-chevron-right"}), this.getTitle())
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