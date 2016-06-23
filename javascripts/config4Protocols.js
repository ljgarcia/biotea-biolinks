var protocolTopics = [
    {id: 'chapter1', text: 'Chapter 1'}
];
protocolTopics = _.sortBy(protocolTopics, function(t) {return t.text;});

var protocolArticles = [
    {topic: 'chapter1', id: 'protocol_01', title: 'Protocol 01'},
    {topic: 'chapter1', id: 'protocol_02', title: 'Protocol 02'},
    {topic: 'chapter1', id: 'protocol_03', title: 'Protocol 03'},
    {topic: 'chapter1', id: 'protocol_04', title: 'Protocol 04'},
    {topic: 'chapter1', id: 'protocol_05', title: 'Protocol 05'},
    {topic: 'chapter1', id: 'protocol_06', title: 'Protocol 06'},
    //{topic: 'chapter1', id: 'protocol_07', title: 'Protocol 07'},
    {topic: 'chapter1', id: 'protocol_08', title: 'Protocol 08'},
    {topic: 'chapter1', id: 'protocol_09', title: 'Protocol 09'},
    {topic: 'chapter1', id: 'protocol_10', title: 'Protocol 10'},
    {topic: 'chapter1', id: 'protocol_11', title: 'Protocol 11'},
    {topic: 'chapter1', id: 'protocol_12', title: 'Protocol 12'},
    {topic: 'chapter1', id: 'protocol_13', title: 'Protocol 13'},
    {topic: 'chapter1', id: 'protocol_14', title: 'Protocol 14'},
    {topic: 'chapter1', id: 'protocol_15', title: 'Protocol 15'},
    {topic: 'chapter1', id: 'protocol_16', title: 'Protocol 16'},
    {topic: 'chapter1', id: 'protocol_17', title: 'Protocol 17'},
    {topic: 'chapter1', id: 'protocol_18', title: 'Protocol 18'},
    {topic: 'chapter1', id: 'protocol_19', title: 'Protocol 19'},
    {topic: 'chapter1', id: 'protocol_20', title: 'Protocol 20'},
    {topic: 'chapter1', id: 'protocol_21', title: 'Protocol 21'},
    {topic: 'chapter1', id: 'protocol_22', title: 'Protocol 22'},
    {topic: 'chapter1', id: 'protocol_23', title: 'Protocol 23'},
    {topic: 'chapter1', id: 'protocol_24', title: 'Protocol 24'},
    {topic: 'chapter1', id: 'protocol_25', title: 'Protocol 25'},
//    {topic: 'chapter1', id: 'protocol_26', title: 'Protocol 26'},
    {topic: 'chapter1', id: 'protocol_27', title: 'Protocol 27'},
//    {topic: 'chapter1', id: 'protocol_28', title: 'Protocol 28'},
//    {topic: 'chapter1', id: 'protocol_29', title: 'Protocol 29'},
    {topic: 'chapter1', id: 'protocol_30', title: 'Protocol 30'},
    {topic: 'chapter1', id: 'protocol_31', title: 'Protocol 31'},
    {topic: 'chapter1', id: 'protocol_32', title: 'Protocol 32'}
];
protocolArticles = _.sortBy(protocolArticles, function(a) {return a.title;});

var ProtocolsConfig = function() {
    var self = this;

    self.content = [
        {label: 'chp1', text: 'Lab protocols', prefix: ''}
    ]

    self.topics = {
        chp1: protocolTopics
    };

    self.articles = {
        chp1: protocolArticles
    };

    return self;
}();