/**
 * @jsx React.DOM
 */
var SearchView = React.createClass({

    getInitialState: function() {
        return {"query": ""};
    },

    searchChange: function(e) {
        this.setState({"query": e.target.value });
        window["searchFor"](e.target.value);
    },

    render: function() {
        return (<div className="search">
                <i className="icon-search"></i>
                <div className="input-container">
                  <input id="search" type="text" placeholder="search for method" onChange={this.searchChange} value={this.state["query"]} />
                </div>
                </div>);
    }
});

var SearchResultsRow = React.createClass({
    rowClick: function(e) {
        if (!e.ctrlKey && !e.metaKey && !window["loadPage"](this.getLink())) {
            e.preventDefault();
            e.stopPropagation();
        }
    },

    getLink: function() {
        return "/" + this.props.version + "/" + this.props.data;
    },
    
    render: function() {
        return (<li>
            <a className="node-link" href={this.getLink()} onClick={this.rowClick}>{this.props.data}</a>
        </li>);
    }
});

var SearchResults = React.createClass({
    render: function() {
        var version = this.props.version;
        if (!this.props.visible) return (<ul style={{"display": "none"}}></ul>);
        if (this.props.results.length > 0) {
            var items = this.props.results.map(function(item) {
                return (<SearchResultsRow data={item} key={item} version={version} />);
            });
            return (<ul>{items}</ul>);
        }else {
            return (<ul><li><a>Nothing found</a></li></ul>);
        }
    }
});
