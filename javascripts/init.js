var init = function() {
    var self = this;
    var app = require('biotea-vis-topicDistribution');

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
            var article = _.find(collection, function(el) {
                return +obj.article === +el.id;
            });
            var articleText = article.title + ' (' +
                (self.selectedContent === 'ta' ? 'PMID' : 'PMC') +
                ':' + article.id + ')';
            self.articleTitle.text(articleText);
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