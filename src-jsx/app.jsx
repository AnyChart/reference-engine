/** @jsx React.DOM */
goog.provide('app.view');
goog.require('goog.dom');

var TreeNode = React.createClass({

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
	
	return <li key={this.props.node["full-name"]}>
	         <a href={this.getLink()}>
	         <i className="fa fa-chevron-right"></i>{this.getTitle()}</a>
	       </li>;
    }
});

var TreeView = React.createClass({
    render: function() {
	var self = this;
	return <ul>{goog.array.map(self.props.tree, function(node) {
	    return <TreeNode key={node["full-name"]} node={node} />;
	})}</ul>;
    }
});