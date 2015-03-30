/**
 * @jsx React.DOM
 */
var SearchView = React.createClass({displayName: "SearchView",

    getInitialState: function() {
        return {"query": ""};
    },

    searchChange: function(e) {
        this.setState({"query": e.target.value });
        window["searchFor"](e.target.value);
    },

    render: function() {
        return (React.createElement("div", {className: "search"}, 
                React.createElement("i", {className: "icon-search"}), 
                React.createElement("div", {className: "input-container"}, 
                  React.createElement("input", {id: "search", type: "text", placeholder: "search for method", onChange: this.searchChange, value: this.state["query"]})
                )
                ));
    }
});

var SearchResultsRow = React.createClass({displayName: "SearchResultsRow",
    componentDidMount: function() {
        var self = this;
        $(this.getDOMNode()).find(">a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return;
            
            return window["loadPage"](self.getLink());
        });
    },

    getLink: function() {
        return "/" + this.props.version + "/" + this.props.data;
    },
    
    render: function() {
        return (React.createElement("li", null, 
            React.createElement("a", {className: "node-link", href: this.getLink()}, this.props.data)
        ));
    }
});

var SearchResults = React.createClass({displayName: "SearchResults",
    render: function() {
        var version = this.props.version;
        if (!this.props.visible) return (React.createElement("ul", {style: {"display": "none"}}));
        if (this.props.results.length > 0) {
            var items = this.props.results.map(function(item) {
                return (React.createElement(SearchResultsRow, {data: item, key: item, version: version}));
            });
            return (React.createElement("ul", null, items));
        }else {
            return (React.createElement("ul", null, React.createElement("li", null, React.createElement("a", null, "Nothing found"))));
        }
    }
});
