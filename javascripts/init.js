var init = function() {
    var self = this;
    var app = require('biotea-vis-topicDistribution');

    self.selectedContent = 'ta';
    self.selectedTopic = '_100';

    self.conSelect = undefined;
    self.topicsSelect = undefined;

    self.contentOptions = undefined;
    self.topicsOption = undefined;

    self.start = function() {
        self.topicDistribution = new app({
            el: '#visDist'
        });
        var controls = d3.select('#controls');

        var contentDiv = controls.append('div');
        contentDiv.append('span').text('Type of content: ');
        self.conSelect = contentDiv.append('span').append('select')
            .attr('id', 'contentSelection')
            .on('change', function() {
                var selectedIndex = self.conSelect.property('selectedIndex')
                self.selectedContent = self.contentOptions[0][selectedIndex].__data__.value;
                self.updateTopics();
            });
        self.contentOptions = self.conSelect.selectAll('option')
            .data(contentType)
            .enter().append('option')
            .attr('value', function(type) {return type.value;})
            .text(function(type) {return type.text;});
        self.conSelect.select('option').attr('selected', 'selected');

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
    };

    return self;
}();