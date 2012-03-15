var filterstat = {"vpd":{"left":0.005980471844821977,"min":0.005980471844821977,"histogram":[633,508,429,420,332,220,188,147,125,104,97,100,66,65,68,57,46,50,60,59,44,53,41,40,48,40,38,26,27,30,20,20,25,24,22,19,22,22,17,18,17,16,15,14,21,14,11,14,12,13,12,10,9,8,6,8,4,5,8,2,5,3,1,1,1,4,2,1,1,0,5,6,4,1,2,1,2,0,1,2,1,0,4,1,2,4,0,0,0,1],"period":0.06091993132465058,"right":2},"ta":{"left":15,"min":-2.9616515837,"histogram":[34,47,41,22,16,12,51,164,206,246,242,326,389,338,388,356,359,457,398,421,402,478,416,457,451,551,560,661,672,699,587,565,510,546,517,572,619,911,925,895,778,796,730,576,632,667,590,554,534,642,553,593,513,449,476,449,403,425,416,385,439,413,399,427,444,416,396,343,279,230,217,201,203,176,143,138,111,128,115,90,75,61,58,18,14,7,11,4,2,1],"period":0.508858239921111,"right":42.8355900092},"rh":{"left":11.2,"min":11.2,"histogram":[5,9,8,11,10,8,10,12,19,20,24,29,29,22,30,25,37,34,32,31,28,31,26,23,36,25,40,29,33,28,38,31,43,27,39,37,28,28,33,31,35,34,24,39,42,45,47,40,45,49,50,41,60,63,85,77,71,83,80,72,75,56,70,48,54,69,48,56,63,67,99,75,73,73,86,89,104,91,101,108,82,116,123,108,112,74,95,149,212,115],"period":0.9788889227977778,"right":99.3000030518},"solar":{"left":100,"min":0.0228881836,"histogram":[5060,169,138,137,123,129,122,97,89,106,104,82,98,96,82,100,80,96,65,89,67,66,54,73,69,68,76,56,55,58,42,51,50,61,46,49,57,63,51,53,49,61,57,55,48,43,50,49,48,54,46,39,40,50,40,49,54,44,48,40,42,41,32,37,34,35,38,31,45,33,38,33,48,38,39,40,41,34,34,34,36,28,22,19,8,2,3,3,2,2],"period":13.333384195963335,"right":1200.0274658203},"wind":{"left":0.0,"min":0.0,"histogram":[13,50,105,105,282,165,137,135,154,291,147,122,111,225,104,103,101,92,189,107,92,84,153,85,74,54,97,99,73,54,55,124,50,52,32,42,66,35,36,20,62,31,30,40,24,47,12,19,23,21,12,5,8,8,26,6,5,5,9,8,3,1,5,5,3,9,2,5,1,1,0,0,0,2,0,0,0,1,0,0,1,1,0,0,0,0,0,1,0,1],"period":0.12222222222222222,"right":2.5},"variety":["Janz [Irrigated]","Hartog [Rainfed]","EGA Gregory [Irrigated]","Hartog [Irrigated]","EGA Gregory [Rainfed]","Janz [Rainfed]"],"date":{"left":14813,"right":14980,"min":14812,"max":14981},"time":{"min":0,"max":1440,"left":1,"right":1439}};
//var canopy = {"Janz [Irrigated]":{"data":[[1.290340635E9,2.64,4.709],[1.290342394E9,3.44,5.043],[1.290343271E9,2.98,4.322],[1.29034415E9,3.66,4.677],[1.290345029E9,3.53,5.094],[1.290345908E9,3.89,5.491],[1.290347666E9,4.47,5.87],[1.290348544E9,4.27,5.228],[1.290349423E9,2.68,4.649],[1.290350302E9,3.56,6.116],[1.29035206E9,4.3,4.517],[1.290352939E9,4.54,6.063],[1.290358212E9,4.01,3.902],[1.290359091E9,4.46,5.097],[1.29043643E9,3.43,4.737],[1.290437309E9,2.62,4.09],[1.290438188E9,3.5,4.797],[1.290439066E9,2.37,4.085],[1.290439945E9,2.79,4.651],[1.290440824E9,4.03,5.665],[1.290441703E9,4.57,5.7],[1.290442582E9,3.41,5.081],[1.290443461E9,4.1,4.995],[1.29044434E9,3.79,4.934],[1.290446097E9,4.83,5.401],[1.290446976E9,4.83,5.438],[1.290453129E9,4.86,3.834],[1.290454007E9,4.72,3.52],[1.290454886E9,4.32,3.162]],"reg_line":[4.099938157109188,0.19853298637961556]},"Hartog [Rainfed]":{"data":[[1.290340803E9,3.89,4.71],[1.29034256E9,4.36,5.248],[1.290343439E9,3.46,4.37],[1.290344318E9,2.87,4.505],[1.290345197E9,4.08,5.197],[1.290346076E9,4.29,5.712],[1.290346954E9,1.83,4.497],[1.290348712E9,4.61,5.914],[1.290349591E9,3.95,5.481],[1.290351349E9,4.33,4.381],[1.290352228E9,2.84,4.643],[1.290358379E9,3.42,3.911],[1.290359258E9,4.97,5.52],[1.290436597E9,3.74,4.967],[1.290437476E9,2.45,4.146],[1.290438355E9,3.9,4.781],[1.290439234E9,2.44,4.273],[1.290440113E9,2.71,4.884],[1.290440991E9,4.17,5.714],[1.290442749E9,3.36,5.148],[1.290443628E9,4.0,4.966],[1.290444507E9,3.44,4.931],[1.290445386E9,4.77,5.443],[1.290446265E9,4.99,5.499],[1.290452417E9,4.82,4.155],[1.290453295E9,4.95,3.937],[1.290454174E9,4.51,3.566],[1.290455053E9,3.79,3.152]],"reg_line":[3.878711076729925,0.2339900886317727]},"EGA Gregory [Irrigated]":{"data":[[1.29034027E9,4.52,4.804],[1.290342907E9,4.55,4.814],[1.290344664E9,4.66,5.585],[1.290347301E9,3.25,5.189],[1.290349059E9,2.68,4.727],[1.290351695E9,3.59,4.305],[1.290352574E9,4.44,5.52],[1.290358726E9,2.52,4.175],[1.290434308E9,4.98,4.837],[1.290436945E9,4.13,4.466],[1.290437823E9,2.87,4.829],[1.290438702E9,2.77,4.171],[1.290439581E9,2.43,4.551],[1.29044046E9,3.29,5.545],[1.290441339E9,3.94,5.581],[1.290442218E9,3.99,5.104],[1.290443097E9,3.51,4.971],[1.290443975E9,3.41,4.929],[1.290444854E9,3.99,5.561],[1.290445733E9,3.78,5.312],[1.290446612E9,4.7,5.725],[1.290447491E9,4.38,5.461],[1.29044837E9,4.28,5.101],[1.290449249E9,4.2,4.885],[1.290450127E9,3.85,4.712],[1.290451006E9,3.89,4.541],[1.290451885E9,4.2,4.322],[1.290452764E9,3.61,3.938],[1.290453643E9,3.3,3.646],[1.290454522E9,3.31,3.337]],"reg_line":[3.3475311120575095,0.39081856405402055]},"Hartog [Irrigated]":{"data":[[1.290340767E9,2.53,3.816],[1.290342525E9,3.11,4.128],[1.290343404E9,2.68,3.651],[1.290344283E9,2.36,3.849],[1.290345161E9,2.94,4.178],[1.29034604E9,3.59,4.64],[1.290346919E9,2.24,3.669],[1.290348677E9,4.0,4.377],[1.290349556E9,2.47,4.156],[1.290350435E9,4.0,5.43],[1.290351313E9,4.02,3.811],[1.290352192E9,2.49,3.843],[1.290353071E9,4.28,5.289],[1.29035395E9,4.33,5.076],[1.290354829E9,4.18,5.003],[1.290355708E9,4.94,5.247],[1.290356587E9,4.88,5.36],[1.290357465E9,4.65,4.925],[1.290358344E9,2.54,3.337],[1.290359223E9,3.59,4.534],[1.290360981E9,4.82,4.708],[1.290368012E9,4.67,2.944],[1.290436562E9,2.97,4.085],[1.290437441E9,1.95,3.51],[1.29043832E9,3.04,3.991],[1.290439198E9,2.26,3.674],[1.290440077E9,2.13,4.029],[1.290440956E9,3.79,4.631],[1.290441835E9,4.14,4.587],[1.290442714E9,3.11,4.354],[1.290443593E9,3.31,4.064],[1.290444472E9,2.84,4.2],[1.29044535E9,3.92,4.481],[1.290446229E9,4.12,4.617],[1.290447108E9,4.57,4.731],[1.290447987E9,4.69,4.554],[1.290448866E9,4.24,4.287],[1.290449745E9,4.09,4.136],[1.290450624E9,4.17,4.0],[1.290451502E9,4.9,3.982],[1.290452381E9,3.96,3.553],[1.29045326E9,3.97,3.379],[1.290454139E9,3.54,3.113],[1.290455018E9,2.89,2.82]],"reg_line":[2.8435607796604434,0.3772001263433035]},"EGA Gregory [Rainfed]":{"data":[],"reg_line":["NaN","NaN"]},"Janz [Rainfed]":{"data":[[1.290274061E9,4.74,4.483],[1.290340854E9,1.53,4.552],[1.290342611E9,2.0,4.88],[1.290344369E9,1.04,4.348],[1.290345248E9,1.93,5.036],[1.290346127E9,3.1,5.449],[1.290347006E9,1.39,4.467],[1.290348764E9,2.35,5.541],[1.290349642E9,2.14,5.199],[1.290350521E9,3.63,5.923],[1.2903514E9,2.93,4.141],[1.290352279E9,1.8,4.57],[1.290353158E9,2.93,5.928],[1.290354037E9,3.47,5.979],[1.290354915E9,3.94,5.627],[1.290355794E9,4.14,5.922],[1.290356673E9,4.52,6.37],[1.290357552E9,4.97,5.803],[1.290358431E9,2.3,3.838],[1.290360189E9,4.79,5.485],[1.290361067E9,4.87,5.373],[1.290361946E9,4.84,5.09],[1.290436649E9,1.85,4.727],[1.290437528E9,0.89,4.069],[1.290438407E9,2.01,4.538],[1.290439285E9,1.12,4.243],[1.290440164E9,0.68,4.897],[1.290441043E9,1.75,5.402],[1.290441922E9,3.05,5.315],[1.290442801E9,1.81,5.13],[1.29044368E9,2.29,4.843],[1.290444559E9,1.8,4.96],[1.290445437E9,2.9,5.29],[1.290446316E9,3.04,5.287],[1.290447195E9,3.39,5.349],[1.290448074E9,3.83,5.165],[1.290448953E9,4.05,5.021],[1.290449832E9,3.93,4.723],[1.290450711E9,3.87,4.458],[1.290451589E9,4.19,4.323],[1.290452468E9,3.74,3.954],[1.290453347E9,3.87,3.738],[1.290454226E9,3.85,3.437],[1.290455105E9,3.18,3.019]],"reg_line":[4.4451858996277585,0.15557149232044387]}}
var colors=pv.Colors.category10().range();
statCalculator= function(data){
    if (data.length==0){
        return {minX:0,minY:0,maxX:0,maxY:0,std:0,avg:0,size:0};
    }
    var maxX = data[0][0];
    var minX = data[0][0];
    var maxY = data[0][1];
    var minY = data[0][1];
    var count = data.length;
    var sum = 0, mEst=0,M2=0;

    for (var idx =0;idx<count;idx++){
        var x = parseFloat(data[idx][0]) ;
        var y = parseFloat(data[idx][1]) ;
        var delta = y - mEst ;
        mEst  = mEst +delta/ (idx+1);
        M2 = M2 + delta *(y-mEst)

        if (x > maxX) maxX = x;
        if (x < minX) minX =x;
        if (y > maxY) maxY = y;
        if (y < minY) minY = y;
        sum += y;
    }
    return {minX:minX,minY:minY,maxX:maxX,maxY:maxY,std:Math.sqrt( M2/(count - 1)),avg:sum/count,size:count};
}

statMerger = function(arrayOfStatObj,statField){
    var maxX = -Infinity;
    var minX = +Infinity;
    var maxY = -Infinity;
    var minY = +Infinity;
    var stdMax = -Infinity;
    var stdMin = +Infinity;
    var meanMax = -Infinity;
    var meanMin = +Infinity;
    for (var idx=0;idx<arrayOfStatObj.length;idx++){
        var data = arrayOfStatObj[idx][statField];
        if (data['maxX'] > maxX) maxX = data['maxX'];
        if (data['maxY'] > maxY) maxY = data['maxY'];
        if (data['minY'] < minY) minY = data['minY'] ;
        if (data['minX'] < minX) minX = data['minX'];

        if (data['std'] < stdMin) stdMin = data['std'] ;
        if (data['std'] > stdMax) stdMax = data['std'];
        if (data['avg'] < meanMin) meanMin = data['avg'] ;
        if (data['avg'] > meanMax) meanMax = data['avg'];
    }
    return {maxX:maxX,minX:minX,maxY:maxY,minY:minY,
        stdMin:stdMin,stdMax:stdMax,meanMin:meanMin,meanMax:meanMax};
}

function fillCTDStatHorizentalBarChart(ctdData){
    var width = 390;
    var height = 130;
    var viz = new pv.Panel()
            .canvas("ctd-stat-table-chart")
            .width(width)
            .height(height)
            .left(10)
            .right(10)
            .bottom(20)

    var merged = statMerger(ctdData,'ctd');
    var meanMinMax = Phenonet.Utils.getNiceMinMax(merged['meanMin']*0.4,merged['meanMax']*1.1,10);
    var stdMinMax = Phenonet.Utils.getNiceMinMax(merged['stdMin']*0.4,merged['stdMax']*1.1,10);
    var meanRange = pv.Scale.linear(meanMinMax[0],meanMinMax[1]).range(0,width/2 -5 );
    var stdRange = pv.Scale.linear(stdMinMax[0],stdMinMax[1]).range(0,width/2 -5);
    var barHeight = height/ctdData.length;

    viz.add(pv.Bar)
            .data(ctdData)
            .title(function(d){return d['name']+":"+d['ctd']['std'];})
            .top(function(){return this.index*barHeight;})
            .height(barHeight-1)
            .right(width/2+5)
            .fillStyle(function(d){return d['color'];})
            .width(function(d){return stdRange(d['ctd']['std']);})
            .anchor("left")
            .add(pv.Label)
            .text(function(d){return d['ctd']['std'].toFixed(3);})
            .textStyle ("#fff")

    viz.add(pv.Bar)
            .data(ctdData)
            .title(function(d){return d['name']+":"+d['ctd']['avg'];})
            .top(function(){return this.index*barHeight;})
            .height(barHeight-1)
            .left(width/2+5)
            .fillStyle(function(d){return d['color'];})
            .width(function(d){return meanRange(d['ctd']['avg']);})
            .anchor("right")
            .add(pv.Label)
            .text(function(d){return d['ctd']['avg'].toFixed(3);})
            .textStyle ("#fff")

    viz.add(pv.Rule)
            .data(stdRange.ticks(5))
            .left(function(d){return width/2 - stdRange(d) -5;})
            .strokeStyle("rgba(255,255,255,0.5)")
            .visible(function(d){return this.index !=0;})
            .add(pv.Rule)
            .bottom(-5)
            .height(5)
            .strokeStyle("#000")
            .anchor("bottom")
            .add(pv.Label)
            .text(function(d){return d.toFixed(2);})

    viz.add(pv.Rule)
            .data(meanRange.ticks(5))
            .left(function(d){return width/2 + meanRange(d) +5;})
            .strokeStyle(function(d){return this.index ==0 ? "#000" : "rgba(255,255,255,0.5)";})
            .visible(function(d){return this.index !=0;})
            .add(pv.Rule)
            .bottom(-5)
            .height(5)
            .strokeStyle("#000")
            .anchor("bottom")
            .add(pv.Label)
            .text(function(d){return d.toFixed(2);})

    viz.add(pv.Rule)
            .data([0])
            .left(width/2)
            .strokeStyle("rgba(0,0,0,0.5)")
            .add(pv.Rule)
            .bottom(-10)
            .anchor("bottom")
//            .add(pv.Label)
//            .strokeStyle("#000")

    viz.add(pv.Rule)
            .data([height])
            .left(0)
            .bottom(-0.5)
            .width(meanRange(meanMinMax[1]) + stdRange(stdMinMax[1])  )
            .strokeStyle("rgba(0,0,0,0.7)")

//            .bottom(0)

    viz.render();
}
function createHistogram(ctdData,barCountPerVariety){
    var merged = statMerger(ctdData,'ctd');
    var min = pv.Scale.linear(merged['minY'],merged['maxY']).nice().ticks()[0]
    var max = pv.Scale.linear(merged['minY'],merged['maxY']).nice().ticks().pop()
    var binSize = (max  -min)/barCountPerVariety;
    var totalMap = [];
    var maxBinSize = -Infinity;
    var minBinSize = +Infinity;
    for (var idx =0;idx<ctdData.length;idx++){
        var varietyMap = [];
        var data = ctdData[idx]['data'];
        for (var valIdx = 0;valIdx<data.length;valIdx++){
            var value = data[valIdx][1];
            var binIdx = (value >= binSize*(barCountPerVariety-1)+min) ? barCountPerVariety -1 : (value < min+binSize) ? 0 : parseInt((value-min)/binSize)
            var toSet = varietyMap[binIdx] == undefined ? 1 : varietyMap[binIdx] + 1
            varietyMap[binIdx] = toSet;
        }
        var tempMinBinSize = pv.min($.grep(varietyMap,function(x){return x!=undefined;}));
        var tempMaxBinSize = pv.max($.grep(varietyMap,function(x){return x!=undefined;}));
        if (tempMinBinSize<minBinSize) minBinSize = tempMinBinSize;
        if (tempMaxBinSize>maxBinSize) maxBinSize =tempMaxBinSize;
        totalMap.push(varietyMap);
    }
    return {binsize:binSize,
        min: min,
        max:max,
        maxY:maxBinSize,
        minY:minBinSize,
        histograms:totalMap
    };
}


function fillCTDStatTable(ctdData){
    var to_return = "";
    for (var idx =0 ;idx < ctdData.length;idx++){
        to_return+= '<tr><td>'+ctdData[idx]['name']+'</td><td>'+ctdData[idx]['ctd']['maxY']+'</td><td>'+ctdData[idx]['ctd']['minY']+'</td><td>'+ctdData[idx]['ctd']['avg'].toFixed(2)+'</td><td>'+ctdData[idx]['ctd']['std'].toFixed(2)+'</td></tr>';
    }
    return to_return;
}

function fillCTDHistogram(ctdData){
    var barCountPerVariety = 20;
    var histograms = createHistogram(ctdData,barCountPerVariety);
    var width = 800;
    var height = 220;
    var viz = new pv.Panel()
            .canvas("ctd-histogram")
            .width(width)
            .height(height)
            .left(30)
            .right(30)
            .top(5)
            .bottom(20)

    var barWidth = width/(ctdData.length*barCountPerVariety);

    var yRange = pv.Scale.linear(histograms['minY'],histograms['maxY']*1).range(0,height);
    var xRange = pv.range(histograms['min'],histograms['max'],histograms['binsize']);
    viz.add(pv.Rule)
            .data(xRange)
            .left(function(){return this.index*barWidth*ctdData.length +1;})
            .bottom(-15)
            .height(15)
            .anchor("right")
            .add(pv.Label)
            .text(function(d){return d.toFixed(2);})


    viz.add(pv.Panel)
            .data(histograms['histograms'])
            .left(function(){return this.index*barWidth;})
            .add(pv.Bar)
            .data(function(d){return d;})
            .width(barWidth)
            .height(function(d){return yRange(d);})
            .bottom(0)
            .left(function(d){return this.index*barWidth*ctdData.length;})
            .fillStyle(function(d){return ctdData[this.parent.index]['color'];})
            .title(function(d){return ctdData[this.parent.index]['name']+":"+d;})
            .text(function(d){return d == undefined ? 0 : d.toFixed(2);})

    viz.add(pv.Rule)
            .data(yRange.ticks())
            .bottom(yRange)
            .strokeStyle("rgba(0,0,0,0.1)")
            .anchor("left")
            .add(pv.Label)
            .anchorTarget()
            .anchor("right")
            .add(pv.Label)
    viz.render();
}

var plotScattered = Phenonet.PhenonetChart.extend({
    paint: function(processed,colors,names,yMinMax){
        var bottom_margin = this.bottom_margin;
        var xRange = this.xRange;
        var viz = this.viz;
        var diff = (yMinMax[1] - yMinMax[0])*0.1;
        var niceMinMax = pv.Scale.linear(yMinMax[0],yMinMax[1]).nice().ticks();
        var yRange = pv.Scale.linear(niceMinMax[0],niceMinMax[niceMinMax.length-1]).range(this.bottom_margin,this.height+this.bottom_margin);
        viz.add(pv.Rule)
                .data(yRange.ticks())
                .strokeStyle(function(){return (this.index ==0 ? "#000" : "#ddd");})
                .bottom(function(d){return yRange(d);})
                .anchor("right")
                .add(pv.Label)
                .text(function(d){return d.toFixed(2);})
                .anchorTarget().anchor("left")
                .add(pv.Label)
                .text(function(d){return d.toFixed(2);})
        var colorIdx = 0 ;
        for (var idx = 0;idx < processed.length;idx++){
            var color = colors[colorIdx];
//            color.opacity=0.6;
            viz.add(pv.Panel)
                    .data(processed[idx])
                    .add(pv.Dot)
                    .shape("cross")
                    .fillStyle(color)
                    .strokeStyle(color)
                    .size(5)
//                    .strokeStyle(null)
                    .left(function(d){return xRange(d[0]);})
                    .bottom(function(d){return yRange(d[1]);})
                    .title(function(d){return Phenonet.Utils.dateTimeFormat(new Date(d[0]*1000))+" - CTD:"+d[1];})
            colorIdx++;
        }

        viz.render();

    }
});

function rgbWithAlpha(rgbString){// "rgb(0, 70, 255)"; // get this in whatever way.

    var parts = rgbString.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/)  ;
// parts now should be ["rgb(0, 70, 255", "0", "70", "255"]
    parts.shift();
    for (var i = 0; i < 3; ++i) {
        parts[i] = parseInt(parts[i]).toString(10);
        if (parts[i].length == 1) parts[i] = '0' + parts[i];
    }
    parts.push("1");
    var color = parts.join(",");
    return "rgba("+color+")";

}


jQuery.fn.timify = function(options){
    var settings = {
        left:0,
        right:24*60-1,
        min:0,
        max:24*60-1, // measured in 15 minute interval
        width:305,
        update:function(){}
    };

    var times = [];
    for (var counter = 0;counter<24*60;counter++){
        var d = new Date(2000,1,1,0,counter,0);
        times.push((d.getHours()<10 ? "0"+d.getHours(): d.getHours())+":"+(d.getMinutes() <10 ? "0"+d.getMinutes():d.getMinutes()));
    }

    return this.each(function(){
        if ( options ) {
            $.extend( settings, options );
        }
        var $this = $(this);

        $this.append("<input type='text' class='range-text left float-left'  />");
        var randomId = "rand_"+parseInt(Math.random()*100000);
        $this.append("<div id='"+randomId+"'class='time-range float-left'  ></div>");
        $this.append("<input type='text' class='range-text right float-left '  />");

        $( "#"+randomId  ).css({width:settings.width,height:settings.height}).slider({
            range: true,
            min: settings.min,
            max: settings.max,
            values:[settings.left,settings.right],
            slide: function( event, ui ) {
                $this.find('input.range-text').first().val(times[ui.values[ 0 ]]);
                $this.find('input.range-text').last().val(times[ui.values[ 1 ]]);
                $this.data('min-val',ui.values[ 0 ]) ;
                $this.data('max-val',ui.values[ 1 ]) ;
            },
            stop: settings['update']
        });
        $this.find('input.range-text').first().val(times[settings.left]);
        $this.find('input.range-text').last().val(times[settings.right]);
    });

}
jQuery.fn.datify = function(options){
    var dayInMSec = 24*60*60*1000;
    var settings = {
        min:0,
        max:1000, // measured in 15 minute interval
        left:0,
        right:1000,
        width:305,
        update:function(){}
    };

    var $this = $(this);
    function updateDate(from,to){
        var fromDate = Phenonet.Utils.dateFormat.format(new Date(from*dayInMSec ));
        var toDate = Phenonet.Utils.dateFormat.format(new Date(to*dayInMSec ));
        $this.find('input.range-text').first().val(fromDate);
        $this.find('input.range-text').last().val(toDate);
    }

    return this.each(function(){
        if ( options ) {
            $.extend( settings, options );
        }
        $this.append("<input type='text' class='range-text left float-left'  />");
        var randomId = "rand_"+parseInt(Math.random()*100000);
        $this.append("<div id='"+randomId+"'class='time-range float-left'  ></div>");
        $this.append("<input type='text' class='range-text right float-left '  />");

        $( "#"+randomId  ).css({width:settings.width,height:settings.height}).slider({
            range: true,
            min: settings.min,
            max: settings.max,
            step: 1, // day in seconds.
            values: [ settings.left, settings.right ],
            slide: function( event, ui ) {
                updateDate(ui.values[ 0 ],ui.values[ 1 ]);
            },
            stop: settings['update']
        });
        updateDate(settings.left,settings.right);
    });

}

jQuery.fn.rangify = function(options){
    var settings = {
        width:150,
        height:25,
        top_margin:0,
        left_margin:10,
        right_margin:10,
        bottom_margin:10,
        update:function(){}
    };

    function drawRangeComponent(randomId,settings,histogram_data,left_function,right_function){
        var viz = new pv.Panel()
                .fillStyle("rgba(0,0,0,0.001)")
                .canvas(randomId)
                .width(settings.width)
                .height(settings.height)
                .left(settings.left_margin)
                .top(settings.top_margin)
                .right(settings.right_margin)
                .bottom(settings.bottom_margin)
                .event("dragend", settings['update'])

        var height = settings.height;
        var width = settings.width;
        var topMargin = settings.top_margin;
        var data = histogram_data['data'];
        var base = histogram_data['min'];
        var period = histogram_data['period'];
        var selectionIdx = [(histogram_data['selection'][0]-base)/period,(histogram_data['selection'][1]-base)/period];
//        alert(histogram_data['selection'][0])
//        alert(histogram_data['selection'][1])
//        alert(histogram_data['selection'][0]-base)
//        alert((histogram_data['selection'][0]-base)/period)
//        var selectionIdx = [histogram_data['selection'][0],histogram_data['selection'][1]];
        var maxFrequency = pv.max(data);
        var yRange= pv.Scale.linear(0,maxFrequency).range(5,settings.height);
        var barWidth = settings.width/data.length ;
        var functions = [left_function,right_function];
        var data2 = $.map(selectionIdx,function(elem,idx){
//            alert((elem-base/period));
            return {index:idx, x:elem*barWidth, y:-settings.topMargin+1 , func:functions[idx]};
        });
//actual histogram
        viz.add(pv.Bar)
                .data(data)
                .width(barWidth  -1)
                .height(yRange)
                .bottom(0)
                .left(function(){return this.index * barWidth});
// bars highlighting uncovered regions
        viz.add(pv.Panel)
                .add(pv.Bar)
                .data(data2)
                .fillStyle("rgba(255,255,255,0.7)")
                .left(function(d){return d.x*d.index;})
                .width(function(d){return (d.index ==0 ? d.x: width-d.x); })
                .height(height)

        var shapeSize = 20;
// The vertical selection range indicators
        viz.add(pv.Panel)
                .width(width)
                .height(height)
                .event("mousedown", pv.Behavior.drag())
                .event("drag", viz)
                .data(data2)
                .add(pv.Rule)
                .top(-topMargin+1)
                .strokeStyle("#555")
                .lineWidth(2)
                .left(function(d){
            d.x = d['index']==0 ? Math.min(data2[1].x+1,d.x) : Math.max(data2[0].x,d.x);
            d['func'](d.x/barWidth*period+base);
            return d.x;
        })
                .height(height+topMargin-2)
                .cursor("move")
                .shapeSize(shapeSize)
                .top(-topMargin+shapeSize/5)
                .add(pv.Dot)
                .fillStyle("#555")
                .strokeStyle(null)
                .shape("triangle")
                .add(pv.Dot)
                .angle(function(){return Math.PI}) //Info: Bug in Protovis 3.3, both rotations are needed !
                .shapeAngle(function(){return Math.PI}) //Info: Bug in Protovis 3.3, both rotations are needed !
                .shape("triangle")
                .top(height+topMargin+3)
        viz.render();
    }

    return this.each(function(){
        if ( options ) {
            $.extend( settings, options );
        }
        var $this = $(this);

        var data = $this.data('histogram');
        if ( !data ) {
            alert("No Data Set for Histogram.");
            return;
        }
        $this.append("<input type='text' class='range-text left float-left'  />");
        var randomId = "rand_"+parseInt(Math.random()*100000);
        $this.append("<div id='"+randomId+"'class='range-histogram float-left'  ></div>");
        $this.append("<input type='text' class='range-text right float-left '  />");
        var leftFunction = function(value){
            $this.find('input.range-text').first().val(value.toFixed(2));
            $this.data('min-val',value.toFixed(2)) ;
        };
        var rightFunction = function(value){
            $this.find('input.range-text').last().val(value.toFixed(2));
            $this.data('max-val',value.toFixed(2)) ;
        };
        drawRangeComponent(randomId, settings,data,leftFunction,rightFunction);

    });
}

$(function(){

    var list = ["rh","solar","wind","ta","vpd"];
//    var list = ["ta"];
    $.each(list, function(idx,place_holder){
        $("#range_"+place_holder).data('histogram',{
            min:filterstat[place_holder]['min'],
            period:filterstat[place_holder]['period'],
            data:filterstat[place_holder]['histogram'],
            selection:[filterstat[place_holder]['left'],filterstat[place_holder]['right']]
        });
        $("#range_"+place_holder).rangify({
            width:318,
            height:20,
            top_margin:0,
            left_margin:10,
            right_margin:10,
            bottom_margin:10,
            update:filterUpdateFinished
        });
    });

    $(".timify").timify({left:filterstat['time']['left'],right:filterstat['time']['right'],update:filterUpdateFinished});
    $(".datify").datify({
        min:filterstat['date']['min'],
        max:filterstat['date']['max'],
        left:filterstat['date']['left'],
        right:filterstat['date']['right'],update:filterUpdateFinished});

    //    var x = new Date();
//    x.setTime( Phenonet.Utils.parseDateToDayIdx("31/03/2011")*1000*60*60*24)
//    alert(x.toString())
    $("#filter-selection .range-text").attr("readonly",true);

    $("#crop-types .checklist input:checkbox").live("change",redrawScatteredGraphs);

    function getFilterValues(){
        var labels = ["rh","solar","wind","ta","vpd","date","time"];
        var to_return = {};
        $.each($("#filter-selection div.filter input.range-text"),function(idx,txtField){
            var labelIdx = parseInt(idx/2);
            var label = labels[labelIdx]+(idx%2==0 ? "Min":"Max");
            var val = txtField.value;
            to_return[label] = (labels[labelIdx] == "date") ? Phenonet.Utils.parseDateToDayIdx(val) : (labels[labelIdx] == "time" ? Phenonet.Utils.parseTimeToDayMinute(val):val);
            //todo: is the order  in DOM directly corresponds to Fields presentation preserved ? In Firefox it is.
        });
        return to_return;
//    document.body.insertBefore( prettyPrint(to_return), document.body.firstChild );

    };

    function processCanopyAnalysis(canopy_analysis){
        var allCtdData =[];
        canopy_analysis = Phenonet.Utils.sortKeysInObject(canopy_analysis);
        var vpdTrendLine = [];
        var totalMinYForTrend = undefined,totalMaxYForTrend = undefined;
        var crop_types = $("#crop-types .checklist");
        if (crop_types.children().length ==0 ){
            jQuery.each(canopy_analysis,function (varietyName,canopyReport){
                var colorIdx = crop_types.find("label").length;
                crop_types.append('<li><label class="float-left" style="border-left-color:'+colors[colorIdx].color+';">'+varietyName+'</label><input type="checkbox" class="float-right "/></li>');
            });
        }
        var crop_types = $("#crop-types .checklist input:checkbox");

        var index = 0;
        jQuery.each(canopy_analysis,function (varietyName,canopyReport){
            var ctdTimeSeries = canopyReport['data'].map(function(x){return [parseInt(x[0])*1000,x[1]]});
            var ctdStat = statCalculator(ctdTimeSeries)
            var trend =canopyReport['reg_line'];
            vpdTrendLine.push(ctdStat['minY'],ctdStat['maxY'],trend[0],trend[1]);
            var ctdReadyForFlot = {
                name:varietyName,
                data:ctdTimeSeries,
                vpdctd:canopyReport['data'].map(function(x){return [x[1],x[2]]}),
                vpdctdLine:  [trend[0],trend[1]],
                color:colors[index].color,
                ctd: ctdStat
            };
            if (totalMaxYForTrend == undefined || ctdStat['minY']<totalMinYForTrend) totalMinYForTrend = ctdStat['minY'];
            if (totalMaxYForTrend == undefined || ctdStat['maxY']>totalMaxYForTrend) totalMaxYForTrend = ctdStat['maxY'];

            $(crop_types[index]).data(ctdReadyForFlot);
            allCtdData.push(ctdReadyForFlot)
            index++;
        });

        $("#crop-types .checklist input:checkbox").map(function(idx,element){
            var trend = $(this).data('vpdctdLine');
            $(this).data('vpdctdLine', [[totalMinYForTrend,trend[0]+trend[1]*totalMinYForTrend],[totalMaxYForTrend,trend[0]+trend[1]*totalMaxYForTrend]]);
        });
        redrawScatteredGraphs();
        $("#ctd-stat-table").html(fillCTDStatTable(allCtdData));
        fillCTDStatHorizentalBarChart(allCtdData);
        fillCTDHistogram(allCtdData);
    }

      function redrawScatteredGraphs(){
        var scatteredSelection = $("#crop-types .checklist input:checked");
        if (scatteredSelection.length == 0 ) {
            $("#canopy-analysis-plots").hide();
            return;
        }
        $("#loader").show();

        var selectionColors = [];
        var selectionTimeSeries = [];
        var selectionCTD2VPD = [];
        var selectionCTD2VPDTrend = [];
        var selectionName = [];
        scatteredSelection.each(function(d){
            selectionTimeSeries.push($(this).data('data'));
            selectionCTD2VPD.push($(this).data('vpdctd'));
            selectionCTD2VPDTrend.push($(this).data('vpdctdLine'));
            selectionColors.push($(this).data('color'));
            selectionName.push($(this).data('name'));
        });
//        var vpdMerged = vpdMergeStat(vpdSet);
//        var scatteredChart = new plotScattered("flot_scattered",50,40,40,20,650,200,[new Date(vpdMerged['minX']),new Date(vpdMerged['maxX'])],  ["CTD[C]","CTD[C]"],[],-45,50,0);
//        scatteredChart.paint(selectionData,selectionColors,selectionName,[vpdMerged['minY'],vpdMerged['maxY']]);
        $("#canopy-analysis-plots").show();
        Phenonet.Utils.plotAccordingToChoices(selectionTimeSeries,"#ctdTimeSeries_holder","#ctdTimeSeries",true,Phenonet.Utils.SCATTERED_TIME_SERIES_GRAPH_OPTIONS,selectionColors);
        Phenonet.Utils.plotAccordingToChoices({data:selectionCTD2VPD, line:selectionCTD2VPDTrend},"#ctd2vpd_holder","#ctd2vpd",true,Phenonet.Utils.SCATTERED_GRAPH_OPTIONS,selectionColors);
        $("#loader").hide();

    }

    function filterUpdateFinished(){
        $.getJSON('/nc/canopy',getFilterValues(),processCanopyAnalysis);
    }

//    $.getJSON('/get_canopy',processCanopyAnalysis);
//    processCanopyAnalysis(canopy);
    filterUpdateFinished();
});



