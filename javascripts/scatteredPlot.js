var BiolinksParser = require("biotea-io-parser");
var parser = new BiolinksParser();

var groups = parser.getModel().getGroups();
groups.unshift('None');

var allArticles = _.filter(pmc_articles, function(article) {
    return article.topic !== '_000';
});

allArticles = _.sortBy(allArticles, function(article) {
    return article.topic + ' ' + article.id;
});
var selectAndLaunch = function(selectedArticleIndex, selectedArticle, selectedGroupValue) {
    var queryArticle = allArticles[selectedArticleIndex];
    var articleTopic = _.find(pubmed_trecTopics, function(topic) {
        return queryArticle.topic === topic.id;
    });
    selectedArticle.text('Topic ' + articleTopic.text + ' (' + queryArticle.topic + ') '
        + queryArticle.title + ' (' + queryArticle.id + ')');

    var selectedGroup = selectedGroupValue === 'None' ? [] : [selectedGroupValue];
    scatteredPlot(queryArticle.id, selectedGroup);
};

var start = function () {
    var selectedArticle = d3.select('#divSelectedArticle');

    var articlesSelect = d3.select('#divArticles').append('select')
        .attr('id', 'articlesSelect')
        .on('change', function() {
            var selectedIndex = articlesSelect.property('selectedIndex');
            var selectedGroupValue = groupsSelect.property('value');
            selectAndLaunch(selectedIndex, selectedArticle, selectedGroupValue);
        });
    articlesSelect.selectAll('option').data(allArticles)
        .enter().append('option')
        .attr('value', function (d) {
            return d.id;
        })
        .text(function(d) {
            return 'topic' + d.topic + ': ' + d.id;
        });


    var groupsSelect = d3.select('#divGroups').append('select')
        .attr('id', 'groupsSelect')
        .on('change', function() {
            var selectedIndex = articlesSelect.property('selectedIndex');
            var queryArticle = allArticles[selectedIndex];
            var selectedGroupValue = groupsSelect.property('value');
            selectAndLaunch(selectedIndex, selectedArticle, selectedGroupValue);
        });
    groupsSelect.selectAll('option').data(groups)
        .enter().append('option')
        .attr('value', function (d) {
            return d;
        })
        .text(function(d) {
            return d;
        });

    articlesSelect.selectedIndex = 0;
    selectAndLaunch(0, selectedArticle, 'None');
};

var calculateSimilarities = function(queryArticle, allRelatedArticles, includedGroups) {
    //use io parser to get all the similarities
    return similarities;
};

var prepareData = function(articleTopics, queryArticle, allRelatedArticles, includedGroups) {
    var similarities = calculateSimilarities(queryArticle, allRelatedArticles, includedGroups);
    _.each(similarities, function(similarity) {
        var articleTopic = _.find(articleTopics, function (article) {
            return article.id === similarity.relatedId;
        });
        similarity.topic = articleTopic.topic;
    });
    return similarities;
};

var loadAnnotations = function(articleType, queryArticleId, allLoaders) {
    var queryArticle = {}, allRelatedArticles = [];
    var loaderPMC, loaderPubMed;
    _.each(allArticles, function(article) {
        var articleAnnotations;
        if (articleType === 'pmc') {
            loaderPMC = parser.loadAnnotations('./pmc/', article.id);
            allLoaders.push(loaderPMC);
            loaderPMC.done(function(loadedData) {
                article.annotationsFT = loadedData.data;
            }).fail( function(e) {
                console.log(e);
            });
        } else {
            loaderPubMed = parser.loadAnnotations('./pubmed-pmc/', article.altId)
            allLoaders.push(loaderPubMed);
            loaderPubMed.done(function(loadedData) {
                article.annotationsTA = loadedData.data;
            }).fail( function(e) {
                console.log(e);
            });
        }
    });
};

//adapted from http://bl.ocks.org/weiglemc/6185069
var scatteredPlot = function(queryArticleId, includedGroups) {
    d3.select("body").select("svg").remove();
    var allLoaders = [];
    loadAnnotations('pmc', queryArticleId, allLoaders);
    loadAnnotations('pubmed', queryArticleId, allLoaders);

    jQuery.when.apply(null, allLoaders)
        .then(function() {
            console.log('all data ready');
            console.log(allArticles);
        }
    );
/*    var similaritiesFT = prepareData(articleTopics, annotationsFT.queryArticle, annotationsFT.allRelatedArticles,
        includedGroups);

    var similaritiesTA = prepareData(articleTopics, annotationsTA.queryArticle, annotationsTA.allRelatedArticles,
        includedGroups);

    _.each(similaritiesFT, function (ft) {
        var simTA = _.find(similaritiesTA, function (ta) {
            return (ft.queryId === ta.queryId) && (ft.relatedId === ta.relatedId);
        });
        ft.fulText = +ft.score;
        ft.titleAbstract = +simTA.score;
    });

    var similarities = _.sortBy(similaritiesFT, function(similarity) {
        return similarity.topic + ' ' + similarity.relatedId;
    });
//margins
    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = 960 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom;

// setup x
    var xValue = function(d) { return d.fullText;}, // data -> value
        xScale = d3.scale.linear().range([0, width]), // value -> display
        xMap = function(d) { return xScale(xValue(d));}, // data -> display
        xAxis = d3.svg.axis().scale(xScale).orient("bottom");

// setup y
    var yValue = function(d) { return d.titleAbstract;}, // data -> value
        yScale = d3.scale.linear().range([height, 0]), // value -> display
        yMap = function(d) { return yScale(yValue(d));}, // data -> display
        yAxis = d3.svg.axis().scale(yScale).orient("left");

// setup fill color
    var cValue = function(d) { return d.topic;},
        color = d3.scale.category20();

// add the graph canvas to the body of the webpage
    var svg = d3.select("body").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// add the tooltip area to the webpage
    var tooltip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    loadScatteredData(svg, similarities);
};


var loadScatteredData = function (svg, similarities) {
    xScale.domain([0, 1]);
    yScale.domain([0, 1]);

    // x-axis
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .append("text")
        .attr("class", "label")
        .attr("x", width)
        .attr("y", -6)
        .style("text-anchor", "end")
        .text("Full text");

    // y-axis
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text("Title and abstract");

    // draw dots
    svg.selectAll(".dot")
        .data(similarities)
        .enter().append("circle")
        .attr("class", "dot")
        .attr("r", 3.5)
        .attr("cx", xMap)
        .attr("cy", yMap)
        .style("fill", function(d) { return color(cValue(d));})
        .on("mouseover", function(d) {
            tooltip.transition()
                .duration(200)
                .style("opacity", .9);
            tooltip.html(d.topic + ": " + d.relatedId + "<br/> (" + xValue(d)
                + ", " + yValue(d) + ")")
                .style("left", (d3.event.pageX + 5) + "px")
                .style("top", (d3.event.pageY - 28) + "px");
        })
        .on("mouseout", function(d) {
            tooltip.transition()
                .duration(500)
                .style("opacity", 0);
        });*/
};