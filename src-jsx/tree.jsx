/**
 * @jsx React.DOM
 */

var nodes = {};

var TreeNode = React.createClass({

    getInitialState: function() {
        return {"collapsed": true};
    },

    getTitle: function() {
        var name = this.props.data["name"];
        switch (this.props.data["kind"]) {
          case "enum": return "[" + name + "]";
          case "typedef": return "{" + name + "}";
          case "function": return name + "()";
          case "method": return name + "()";
          default: return name;
        }
    },

    getClass: function() {
        return this.props.data["kind"];
    },

    isGroup: function() {
        var kind = this.props.data["kind"];
        return kind == "namespace" || kind == "class";
    },

    getLink: function() {
        return "/" + this.props.version + "/" + this.props.data["full-name"];
    },

    isExpanded: function() {
        return !this.state["collapsed"];
    },

    getIcon: function() {
        if (!this.isGroup()) return null;
        var className = this.isExpanded() ? "icon-down-open" : "icon-right-open";
            return (<i className={className}></i>);
    },

    componentDidMount: function() {
        var self = this;
        $(this.getDOMNode()).find(">a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return;
            
            if (self.isGroup())
                self.setState({"collapsed": self.isExpanded()});
            
            return window["loadPage"](self.getLink());
        });
    },
    
    render: function() {
        var subnodes = null;
        var version = this.props.version;
        if (this.isGroup() && this.isExpanded())
            subnodes = (<ul>{this.props.data.children.map(function(data) {
                return (<TreeNode key={data["full-name"]} data={data} version={version} />);
            })}</ul>);

        nodes[this.getLink()] = this;
        
        return (<li className={this.getClass()}>
            <a href={this.getLink()}>{this.getIcon()}{this.getTitle()}</a>
            {subnodes}
                </li>);
    }
});

var TreeView = React.createClass({
    render: function() {
        var version = this.props.version;
        var page = this.props.page;
        var rootNodes = this.props.data.map(function(data) {
            return (<TreeNode key={data["full-name"]} data={data} version={version} />);
        });
        return (<ul>{rootNodes}</ul>);
    }
});
