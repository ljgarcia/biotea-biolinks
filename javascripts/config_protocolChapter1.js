var protocolTopics = [
      {value: 'chapter1', text: 'Chapter 1'}
  ];
protocolTopics = _.sortBy(protocolTopics, function(t) {return t.text;});

var protocolArticles = [
    {topic: 'chapter1', id: 'chapter1protocol01.ann', title: 'Protocol 01'},
    {topic: 'chapter1', id: 'chapter1protocol02.ann', title: 'Protocol 02'},
    {topic: 'chapter1', id: 'chapter1protocol03.ann', title: 'Protocol 03'},
    {topic: 'chapter1', id: 'chapter1protocol04.ann', title: 'Protocol 04'},
    {topic: 'chapter1', id: 'chapter1protocol05.ann', title: 'Protocol 05'},
    {topic: 'chapter1', id: 'chapter1protocol06.ann', title: 'Protocol 06'},
    //{topic: 'chapter1', id: 'chapter1protocol07.ann', title: 'Protocol 07'},
    {topic: 'chapter1', id: 'chapter1protocol08.ann', title: 'Protocol 08'},
    {topic: 'chapter1', id: 'chapter1protocol09.ann', title: 'Protocol 09'},
    {topic: 'chapter1', id: 'chapter1protocol10.ann', title: 'Protocol 10'},
    {topic: 'chapter1', id: 'chapter1protocol11.ann', title: 'Protocol 11'},
    {topic: 'chapter1', id: 'chapter1protocol12.ann', title: 'Protocol 12'},
    {topic: 'chapter1', id: 'chapter1protocol13.ann', title: 'Protocol 13'},
    {topic: 'chapter1', id: 'chapter1protocol14.ann', title: 'Protocol 14'},
    {topic: 'chapter1', id: 'chapter1protocol15.ann', title: 'Protocol 15'},
    {topic: 'chapter1', id: 'chapter1protocol16.ann', title: 'Protocol 16'},
    {topic: 'chapter1', id: 'chapter1protocol17.ann', title: 'Protocol 17'},
    {topic: 'chapter1', id: 'chapter1protocol18.ann', title: 'Protocol 18'},
    {topic: 'chapter1', id: 'chapter1protocol19.ann', title: 'Protocol 19'},
    {topic: 'chapter1', id: 'chapter1protocol20.ann', title: 'Protocol 20'},
    {topic: 'chapter1', id: 'chapter1protocol21.ann', title: 'Protocol 21'},
    {topic: 'chapter1', id: 'chapter1protocol22.ann', title: 'Protocol 22'},
    {topic: 'chapter1', id: 'chapter1protocol23.ann', title: 'Protocol 23'},
    {topic: 'chapter1', id: 'chapter1protocol24.ann', title: 'Protocol 24'},
    {topic: 'chapter1', id: 'chapter1protocol25.ann', title: 'Protocol 25'},
//    {topic: 'chapter1', id: 'chapter1protocol26.ann', title: 'Protocol 26'},
    {topic: 'chapter1', id: 'chapter1protocol27.ann', title: 'Protocol 27'},
//    {topic: 'chapter1', id: 'chapter1protocol28.ann', title: 'Protocol 28'},
//    {topic: 'chapter1', id: 'chapter1protocol29.ann', title: 'Protocol 29'},
    {topic: 'chapter1', id: 'chapter1protocol30.ann', title: 'Protocol 30'},
    {topic: 'chapter1', id: 'chapter1protocol31.ann', title: 'Protocol 31'},
    {topic: 'chapter1', id: 'chapter1protocol32.ann', title: 'Protocol 32'}
];
protocolArticles = _.sortBy(protocolArticles, function(a) {return a.title;});