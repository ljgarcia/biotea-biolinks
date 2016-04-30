var init = function() {
    var self = this;
    var appTopic = require('biotea-vis-topicDistribution');
    var appSimilarity = require('biotea-vis-similarity');
    var appAnnotation = require('biotea-vis-annotation');

    self.selectedContent = 'ta';
    self.selectedTopic = '_100';

    self.contentSelect = undefined;
    self.topicsSelect = undefined;

    self.contentOptions = undefined;
    self.topicsOption = undefined;
    self.articleTitle = undefined;
    self.similarity = undefined;
    self.similarityTA = undefined;

    self.start = function() {
        self.topicDistribution = new appTopic({
            el: '#visDist',
            width: 880,
            prefixId: 'PMC'
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
        self.contentSelect.selectAll('option:nth-child(2)').attr('selected', 'selected');
        self.selectedContent = 'fc';

        var topicsDiv = controls.append('div');
        topicsDiv.append('span').text('TREC topic: ');
        self.topicsSelect = topicsDiv.append('span').append('select')
            .attr('id', 'topicsSelection')
            .on('change', function() {
                var selectedIndex = self.topicsSelect.property('selectedIndex')
                self.selectedTopic = self.topicsOption[0][selectedIndex].__data__.value;
                self.updateDistribution(selectedIndex);
            });

        var articleDiv = d3.select('#selectedArticle');
        articleDiv.html('');
        articleDiv.append('span').text('Selected Article: ');
        self.articleTitle = articleDiv.append('span').text('Click on any column in the distribution matrix to select'
            + 'an article');

        self.updateTopics();

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

            self.updateSimilarity(selectedArticle);
        });
    };

    self.updateAnnotation = function(ftId, taId, articles) {
        d3.select('#annotGroup').style('display', 'block');
        d3.select('#annotatedArticle').html('');

        d3.select('#annotatedArticle').html(function() {
            var annotArt = _.find(articles, function(art) {
                return art.id === ftId;
            });
            return 'Annotated article: ' + annotArt.title + ' (PMC:' + annotArt.id + ')'
        });

        d3.select('#visAnnotation').selectAll('*').remove();
        d3.select('#visAnnotation').html('');
        var annotation = new appAnnotation({
            el: '#visAnnotation',
            width: 400,
            height: 500,
            path: './pmc/',
            id: ftId
        });

        d3.select('#visAnnotationTA').selectAll('*').remove();
        d3.select('#visAnnotationTA').html('');
        var annotationTA = new appAnnotation({
            el: '#visAnnotationTA',
            width: 400,
            height: 400,
            translation: -100,
            path: './pubmed-pmc/',
            id: taId
        });
    };

    self.createSimilarity = function(similarity, elSimilarityId, opts) {
        if (similarity) {
            similarity.stopForce();
        }
        d3.select('#' + elSimilarityId).selectAll('*').remove();
        d3.select('#' + elSimilarityId).html('');
        var simil = new appSimilarity(opts);
        return simil;
    };

    self.updateSimilarity = function(selectedArticle) {
        var topic = selectedArticle.topic.replace('_', 'T');
        var pathTA, pathFT, articles;
        if (selectedContent === 'ta') {
            return;
        } else {
            pathFT = './pmc/' + topic + '/';
            pathTA = './pubmed-pmc/' + topic + '/';
            articles = _.filter(pmc_articles, function(elem) {
                return elem.topic === selectedArticle.topic;
            });
        }

        if (articles.length >= 3) {
            var relatedIdsFT = [];
            var relatedIdsTA = [];
            _.each(articles, function(elem) {
                if (elem.id !== selectedArticle.id) {
                    relatedIdsFT.push(elem.id);
                    relatedIdsTA.push(elem.pmid);
                }
            });

            self.similarity = self.createSimilarity(self.similarity, 'visSimilarity',
                {
                    el: '#visSimilarity', width: 400, height: 400,
                    path: pathFT,
                    queryId: selectedArticle.id, prefixId: "PMC", relatedIds: relatedIdsFT
                }
            );

            self.similarity.getDispatcher().on('selected', function(obj) {
                var pmid = _.find(articles, function(art) {
                    return art.id === obj.datum.relatedId;
                }).pmid;
                self.updateAnnotation(obj.datum.relatedId, pmid, articles);
            });

            self.similarityTA = self.createSimilarity(self.similarityTA, 'visSimilarityTA',
                {
                    el: '#visSimilarityTA', width: 400, height: 400,
                    path: pathTA, alternativeQueryId: selectedArticle.id,
                    queryId: selectedArticle.pmid, prefixId: "PMID", relatedIds: relatedIdsTA,
                    useAlternativeIds: true, alternativePrefixId: "PMC", alternativeRelatedIds: relatedIdsFT
                }
            );

            self.similarityTA.getDispatcher().on('selected', function(obj) {
                var pmcid = _.find(articles, function(art) {
                    return art.id === obj.datum.altId;
                }).id;
                self.updateAnnotation(pmcid, obj.datum.relatedId, articles);
            });

            d3.select('#clickNode').style('display', 'block');
        }
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

        self.topicsSelect.select('option:nth-child(2)').attr('selected', 'selected');
        self.updateDistribution(1);
    };

    self.updateDistribution = function(selectedIndex) {
        var path, topics, articles, prefix;
        if (selectedContent === 'ta') {
            path = './pubmed/';
            topics = pubmed_trecTopics;
            articles = pubmed_articles;
            prefix = 'PMID';
        } else {
            path = './pmc/'
            topics = pmc_trecTopics;
            articles = pmc_articles;
            prefix = 'PMC';
        }

        var topicArticles = _.filter(articles, function(art) {
            return art.topic === topics[selectedIndex].value;
        });
        var topicIds = _.pluck(topicArticles, 'id');

        self.topicDistribution.setPath(path);
        self.topicDistribution.setIds(topicIds);
        self.topicDistribution.setPrefix(prefix);
        self.topicDistribution.render();

        if (self.articleTitle !== undefined) {
            if ((selectedContent !== 'ta') && (topicArticles.length >= 3)) {
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