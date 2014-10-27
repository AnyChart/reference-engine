/** @jsx React.DOM */
goog.provide('app.view');
goog.require('goog.dom');
goog.require('goog.style');

var SearchField = React.createClass({displayName: 'SearchField',

    getInitialState: function() {
	return { searchVisible: false };
    },

    search: function(e) {
	var query = e.target.value;
	if (query.length) {
	    query = query.toLowerCase();
	    this.setState({results: goog.array.filter(this.props.index, function(row) {
		return row.toLowerCase().indexOf(query) != -1;
	    }), searchVisible: true});
	}else {
	    this.setState({results: null});
	}
    },

    hideSearch: function() {
	this.setState({searchVisible: false});
    },

    componentDidMount: function() {
	 goog.events.listen(document, goog.events.EventType.CLICK, this.hideSearch);
    },

    componentDidUnmount: function() {
	goog.events.unlisten(document, goog.events.EventType.CLICK, this.hideSearch);
    },

    showSearch: function(e) {
	this.search(e);
    },

    onLinkClick: function(e) {
	var res = app.loadPage(e.target.getAttribute("href"), e);
	this.setState({searchVisible: false});
	return res;
    },

    getLink: function(title) {
	if (app.project != null)
	    return "/" + app.project + "/" + app.version + "/" + title;
	return "/" + title;
    },

    render: function() {
	var results = null;
	var self = this;
	if (this.state.results && this.state.searchVisible) {
	    if (this.state.results.length) {
		results = React.DOM.ul({className: "search-results"}, 
		  goog.array.map(this.state.results, function(row) {
		      return React.DOM.li({key: row}, React.DOM.a({href: self.getLink(row), onClick: self.onLinkClick}, row));
		  })
		);
	    }else {
		results = React.DOM.ul({className: "search-results"}, 
		  React.DOM.li(null, React.DOM.a(null, "Nothing found"))
		);
	    }
	}
	
	return (React.DOM.div({className: "search"}, 
		  React.DOM.i({className: "fa fa-search"}), 
		  React.DOM.div(null, 
		    React.DOM.input({type: "text", ref: "query", placeholder: "search for method in the tree", onChange: this.search, onFocus: this.showSearch})
		  ), 
		  results
		));
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
	if (node.children && this.state.visible) {
	    var hash = {};
	    
	    children = React.DOM.ul({ref: "list"}, 
	      goog.array.map(node.children, function(node) {
		  if (hash[node["full-name"]]) return null;
		  hash[node["full-name"]] = true;
		  return TreeNode({key: node["full-name"], node: node});
	      })
	    );
	}
	
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
	goog.array.sortObjectsByKey(self.props.tree, "full-name");

	var hash = {};
	
	return React.DOM.ul(null, goog.array.map(self.props.tree, function(node) {
	    if (hash[node["full-name"]]) return null;
	    hash[node["full-name"]] = true;
	    return TreeNode({key: node["full-name"], node: node});
	}));
    }
});