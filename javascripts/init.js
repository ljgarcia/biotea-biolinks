var init = function() {
    var self = this;
    var app = require('biotea-vis-topicDistribution');
    var appSimilarity = require('biotea-vis-similarity');

    self.selectedContent = 'ta';
    self.selectedTopic = '_100';

    self.contentSelect = undefined;
    self.topicsSelect = undefined;

    self.contentOptions = undefined;
    self.topicsOption = undefined;
    self.articleTitle = undefined;

    self.start = function() {
        self.topicDistribution = new app({
            el: '#visDist'
        });

        var controls = d3.select('#controls');

        var contentDiv = controls.append('div');
        contentDiv.append('span').text('Type of content: ');
        self.contentSelect = contentDiv.append('span').append('select')
            .attr('id', 'contentSelection')
            .on('change', function() {
                var selectedIndex = self.contentSelect.property('selectedIndex')
                self.selectedContent = self.contentOptions[0][selectedIndex].__data__.value;
                self.updateTopics();
            });
        self.contentOptions = self.contentSelect.selectAll('option')
            .data(contentType)
            .enter().append('option')
            .attr('value', function(type) {return type.value;})
            .text(function(type) {return type.text;});
        self.contentSelect.select('option').attr('selected', 'selected');

        var topicsDiv = controls.append('div');
        topicsDiv.append('span').text('TREC topic: ');
        self.topicsSelect = topicsDiv.append('span').append('select')
            .attr('id', 'topicsSelection')
            .on('change', function() {
                var selectedIndex = self.topicsSelect.property('selectedIndex')
                self.selectedTopic = self.topicsOption[0][selectedIndex].__data__.value;
                self.updateDistribution(selectedIndex);
            });

        self.updateTopics();

        var articleDiv = controls.append('div');
        articleDiv.append('span').text('Selected Article: ');
        self.articleTitle = articleDiv.append('span').text('Click on any column to select an article');

        self.topicDistribution.getDispatcher().on('selected', function(obj) {
            var collection;
            if (self.selectedContent === 'ta') {
                collection = pubmed_articles;
            } else {
                collection = pmc_articles;
            }
            var selectedArticle = _.find(collection, function(el) {
                return +obj.article === +el.id;
            });
            var articleText = selectedArticle.title + ' (' +
                (self.selectedContent === 'ta' ? 'PMID' : 'PMC') +
                ':' + selectedArticle.id + ')';
            self.articleTitle.text(articleText);

            var topic = selectedArticle.topic.replace('_', 'T');
            var path, db, articles;
            if (selectedContent === 'ta') {
                db = 'PubMed';
                path = './pubmed-pmc/' + topic + '/';
                articles = _.filter(pubmed_articles, function(elem) {
                    return elem.topic === selectedArticle.topic;
                });
            } else {
                db = 'PMC';
                path = './pmc/' + topic + '/';
                articles = _.filter(pmc_articles, function(elem) {
                    return elem.topic === selectedArticle.topic;
                });
            }
            var relatedIds = []
            _.each(articles, function(elem) {
                if (elem.id !== selectedArticle.id) {
                    relatedIds.push(elem.id);
                }
            });
            self.similarity = new appSimilarity({
                el: '#visSimilarity',
                path: path,
                queryId: selectedArticle.id,
                db: db,
                relatedIds: relatedIds
            });
        });
    };

    self.updateTopics = function() {
        var topics;
        if (self.selectedContent === 'ta') {
            topics = pubmed_trecTopics;
        } else {
            topics = pmc_trecTopics;
        }

        self.topicsSelect.selectAll('option').remove();
        self.topicsOption = self.topicsSelect.selectAll('option')
            .data(topics)
            .enter().append('option')
            .attr('value', function(topic) {return topic.value;})
            .text(function(topic) {return topic.text;});

        self.topicsSelect.select('option').attr('selected', 'selected');
        self.updateDistribution(0);
    };

    self.updateDistribution = function(selectedIndex) {
        var path, topics, articles;
        if (selectedContent === 'ta') {
            path = './pubmed/';
            topics = pubmed_trecTopics;
            articles = pubmed_articles;
        } else {
            path = './pmc/'
            topics = pmc_trecTopics;
            articles = pmc_articles;
        }

        var topicArticles = _.filter(articles, function(art) {
            return art.topic === topics[selectedIndex].value;
        });
        var topicIds = _.pluck(topicArticles, 'id');

        self.topicDistribution.setPath(path);
        self.topicDistribution.setIds(topicIds);
        self.topicDistribution.render();
        if (self.articleTitle !== undefined) {
            self.articleTitle.text('Click on any column to select an article');
        }
    };

    return self;
}();