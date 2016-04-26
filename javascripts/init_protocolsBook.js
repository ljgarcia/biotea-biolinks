var init = function() {
    var self = this;
    var appTopic = require('biotea-vis-topicDistribution');
    var appSimilarity = require('biotea-vis-similarity');
    var appAnnotation = require('biotea-vis-annotation');

    self.selectedTopic = 'chapter1';
    self.topicsSelect = undefined;
    self.topicsOption = undefined;
    self.articleTitle = undefined;
    self.similarity = undefined;

    self.start = function() {
        self.topicDistribution = new appTopic({
            el: '#visDist',
            width: 880
        });

        var controls = d3.select('#controls');

        var topicsDiv = controls.append('div');
        topicsDiv.append('span').text('Chapter: ');
        self.topicsSelect = topicsDiv.append('span').append('select')
            .attr('id', 'topicsSelection')
            .on('change', function() {
                var selectedIndex = self.topicsSelect.property('selectedIndex')
                self.selectedTopic = self.topicsOption[0][selectedIndex].__data__.value;
                self.updateDistribution(selectedIndex);
            });
        self.topicsOption = self.topicsSelect.selectAll('option')
            .data(protocolTopics)
            .enter().append('option')
            .attr('value', function(topic) {return topic.value;})
            .text(function(topic) {return topic.text;});

        var articleDiv = d3.select('#selectedArticle');
        articleDiv.html('');
        articleDiv.append('span').text('Selected Article: ');
        self.articleTitle = articleDiv.append('span').text('Click on any column in the distribution matrix to select'
            + 'an article');

        self.updateDistribution(0);

        self.topicDistribution.getDispatcher().on('selected', function(obj) {
            var selectedArticle = _.find(protocolArticles, function(el) {
                return obj.article === el.id;
            });
            self.articleTitle.text(selectedArticle.title);

            self.updateSimilarity(selectedArticle);
        });
    };

    self.updateAnnotation = function(id, articles) {
        d3.select('#annotGroup').style('display', 'block');
        d3.select('#annotatedArticle').html('');

        d3.select('#annotatedArticle').html(function() {
            var annotArt = _.find(articles, function(art) {
                return art.id === id;
            });
            return 'Annotated article: ' + annotArt.title;
        });

        d3.select('#visAnnotation').selectAll('*').remove();
        d3.select('#visAnnotation').html('');

        var correctionX = Math.abs(Math.floor(
            (document.documentElement.clientWidth - parseInt(d3.select('.wrapper').style('width'))) / 2
        ));
        var correctionY = Math.abs(Math.floor(
            (parseInt(d3.select('.wrapper').style('height')) - parseInt(d3.select('section').style('height'))) / 2
        ));
        var annotation = new appAnnotation({
            el: '#visAnnotation',
            width: 880,
            height: 500,
            correctionX: -correctionX,
            correctionY: -100,
            path: './protocols/' + self.selectedTopic + '/annotations/',
            id: id
        });
    };

    self.updateSimilarity = function(selectedArticle) {
        var path = './protocols/' + selectedArticle.topic + '/similarity/';
        articles = _.filter(protocolArticles, function(elem) {
            return elem.topic === selectedArticle.topic;
        });

        if (articles.length >= 3) {
            var relatedIds = [];
            var altIds = [];
            _.each(articles, function(elem) {
                if (elem.id !== selectedArticle.id) {
                    relatedIds.push(elem.id);
                    altIds.push(elem.title);
                }
            });

            if (self.similarity) {
                self.similarity.stopForce();
            }
            d3.select('#visSimilarity').selectAll('*').remove();
            d3.select('#visSimilarity').html('');
            self.similarity = new appSimilarity({
                el: '#visSimilarity', width: 880, height: 400,
                path: path, alternativeRelatedIds: altIds, useAlternativeIds: true,
                queryId: selectedArticle.id, prefixId: "", relatedIds: relatedIds, alternativePrefixId: ""
            });

            self.similarity.getDispatcher().on('selected', function(obj) {
                self.updateAnnotation(obj.datum.relatedId, articles);
            });

            d3.select('#clickNode').style('display', 'block');
        }
    };

    self.updateDistribution = function(selectedIndex) {
        var path = './protocols/' + self.selectedTopic + '/distribution/';
        var topics = protocolTopics;
        var articles = protocolArticles;

        var topicArticles = _.filter(articles, function(art) {
            return art.topic === topics[selectedIndex].value;
        });
        var topicIds = _.pluck(topicArticles, 'id');
        var topicsDisplay = _.pluck(topicArticles, 'title');

        self.topicDistribution.setPath(path);
        self.topicDistribution.setIds(topicIds, topicsDisplay);
        self.topicDistribution.render();

        if (self.articleTitle !== undefined) {
            if (topicArticles.length >= 3) {
                d3.select('#simGroup').style('display', 'block');
                self.articleTitle.text('Click on any column in the distribution matrix to select an article and ' +
                    'display similarity network');
            } else {
                d3.select('#simGroup').style('display', 'none');
                self.articleTitle.text('Click on any column in the distribution matrix to select an article');
            }
            d3.select('#annotGroup').style('display', 'none');
            d3.select('#clickNode').style('display', 'none');
        }
    };

    return self;
}();