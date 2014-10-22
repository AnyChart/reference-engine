/** @jsx React.DOM */
goog.provide('app.view');
goog.require('goog.dom');
goog.require('goog.style');

var TreeNode = React.createClass({

    getInitialState: function() {
	return { visible: false };
    },

    getTitle: function() {
	switch (this.props.node.kind) {
	    case "namespace": return this.props.node["full-name"];
	    default: return this.props.node["name"];
	}
    },

    getLink: function() {

	var kind = this.props.node.kind;
	var isTopLevel = goog.array.contains(["namespace", "class", "typedef", "enum"], kind);
	var name = this.props.node["full-name"];
	if (!isTopLevel) {
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
		icon = <i className="fa fa-chevron-down"></i>;
	    }else {
		icon = <i className="fa fa-chevron-right"></i>;
	    }
	

	var children = null;
	if (node.children && this.state.visible)
	    children = <ul ref="list">
	      {goog.array.map(node.children, function(node) {
		  return <TreeNode key={node["full-name"]} node={node} />;
	      })}
	    </ul>;
	
	return <li key={this.props.node["full-name"]}>
	         <a href={this.getLink()} onClick={this.toggleTree}>
	         {icon}{this.getTitle()}</a>
	         {children}
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