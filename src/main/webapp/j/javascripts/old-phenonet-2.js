$(function(){
    $.ajaxSetup({
        type:'get',
        timeout:30000,
        dataType:'json'
    });
    $(document).ajaxStart(function(){$("#loader").show();});
    $(document).ajaxComplete(function(){$("#loader").hide();});
    $(document).ajaxError(function(e, xhr, settings, exception) {
        alert('error in: ' + settings.url + ' \n'+'error:\n' + xhr.responseText );
    });

});

var DefaultComparisonSignalLabel = "Select A Signal";

var plotEt0ToVPD = Phenonet.PhenonetChart.extend({
    paint: function(processed){
        var dates = this.dates;
        var dateObj = this.dateObj;
        var axis_names = this.axis_names;
        var colors = this.colors;
        var width = this.width;
        var top_margin = this.top_margin;
        var xRange = this.xRange;
        var viz = this.viz;

        var rain = processed['rain']
        var vpd = processed['vpd'];
        var et0 = processed['et0'];

        var maxEt0 = pv.max(et0);
        var maxVPD = pv.max(vpd);
        var maxRain = pv.max(rain);

        var y1Range = pv.Scale.linear(0,this.getNiceTail(0,maxEt0*1.1)).range(this.bottom_margin,this.height+this.bottom_margin);
        var y2Range = pv.Scale.linear(0,this.getNiceTail(0,maxVPD*1.1)).range(this.bottom_margin,this.height+this.bottom_margin);
        var rainRange  = pv.Scale.linear(0,maxRain*1.1).range(0,this.height);

        var actualRoundedHeightStart = y1Range.invert(y1Range.ticks()[0]);
        var actualRoundedHeightEnd = y1Range.invert(y1Range.ticks()[y1Range.ticks().length-1]);
        // Y-Axis
        viz.add(pv.Rule)
                .data(y1Range.ticks())
                .strokeStyle(function(){return (this.index ==0 ? "#000" : "#ddd");})
                .bottom(y1Range)
                .anchor("left")
                .add(pv.Label)
                .text(function(d){return d.toFixed(2);})
                .anchorTarget().anchor("right")
                .add(pv.Label)
                .text(function(d){return y2Range.invert(y1Range(d)).toFixed(2);})

        var rainColor = (this.colors[2]);
        rainColor.opacity=0.3;

        var rainPanel = viz.add(pv.Panel)
                .add(pv.Bar)
                .data(rain)
                .width(this.width/(rain.length)-1)
                .height(rainRange)
                .bottom(this.bottom_margin)
                .fillStyle(rainColor)
                .title(function(d){return "Rain: "+d+"mm measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})
                .left(function(d){return xRange(dates[this.index]);})
                .anchor("top")
                .textBaseline("bottom")
                .add(pv.Label)
                .visible(function(d){return parseFloat(d)>5})
                .text(function(d){return d+"mm";})

        var panel = viz.add(pv.Panel).data([et0,vpd]);

        var line = panel.add(pv.Line)
                .data(function(d){ return d;})
                .left(function(){return xRange(dates[this.index]);})
                .bottom(function(d){return this.parent.index ==0 ? y1Range(d) :y2Range(d);})
//                .interpolate("basis")
//            .interpolate("step-before")
                .title(function(d){return axis_names[this.parent.index]+": "+ d+" measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})

        var dot = line.add(pv.Dot)
                .strokeStyle(null)
                .fillStyle(function(){return line.strokeStyle();})
                .shapeSize(5) // was .size(5) for 3.2

        viz.render();
    }
});

var plotTaToRa = Phenonet.PhenonetChart.extend({
    paint: function(processed){
        var dates = this.dates;
        var dateObj = this.dateObj;
        var axis_names = this.axis_names;
        var colors = this.colors;
        var width = this.width;
        var top_margin = this.top_margin;
        var xRange = this.xRange;
        var viz = this.viz;

        var rh = processed['rh'];
        var tMax = processed['temperatureMax'];
        var tMin = processed['temperatureMin'];

        var maxRh = 100;
        var maxTa = pv.max(tMax);
        var minTa = pv.min(tMin);


        var y1Range = pv.Scale.linear(minTa-(maxTa-minTa)*.1,maxTa*1.1).range(this.bottom_margin,this.height+this.bottom_margin);
        var rhRange = pv.Scale.linear(0,100).range(this.bottom_margin,this.height+this.bottom_margin);

        // Y-Axis
        viz.add(pv.Rule)
                .data(pv.range(0,101,10))
                .strokeStyle(function(){return (this.index ==0 ? "#000" : "#ddd");})
                .bottom(rhRange)
                .anchor("right")
                .add(pv.Label)
                .text(function(d){return d.toFixed(2);})
                .anchorTarget().anchor("left")
                .add(pv.Label)
                .text(function(d){return y1Range.invert(rhRange(d)).toFixed(2);})

        // Rh Chart
        var taColor = this.colors[0];
        taColor.opacity = 0.3;
        var rhColor = this.colors[1];

        var lineRh = viz.add(pv.Line)
                .data(rh)
                .bottom(rhRange)
//                .interpolate("basis")
                .strokeStyle(rhColor)
                .title(function(d){return "RH: "+d+"% measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})
                .left(function(d){return xRange(dates[this.index]);})

        var lineRhDots  = lineRh.add(pv.Dot)
                .strokeStyle(null)
                .fillStyle(function(){return lineRh.strokeStyle();})
                .shapeSize(5) // was .size(5) for 3.2

        var lineTa = viz.add(pv.Dot)
                .data(tMax)
                .left(function(d){return xRange(dates[this.index]);})
                .shapeSize(5) // was .size(5) for 3.2
                .strokeStyle(null)
                .fillStyle(taColor)
                .bottom(y1Range)
                .title(function(d){return "Temperature Max:"+d+" measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})
                .add(pv.Area)
                .bottom(function(d){return y1Range(tMin[this.index]);})
//                .interpolate("basis")
                .title(function(d){return "Temperature Min:"+tMin[this.index]+" measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})
                .height(function(d){return y1Range(d) - y1Range(tMin[this.index]);})
                .add(pv.Dot)

        viz.render();
    }
});

var plotDegreeDays = Phenonet.PhenonetChart.extend({
    paint: function(processed){
        var dates = this.dates;
        var bottom_margin = this.bottom_margin;
        var dateObj = this.dateObj;
        var colors = this.colors;
        var width = this.width;
        var height = this.height;
        var xRange = this.xRange;
        var viz = this.viz;

        var ddValues = [];
        var currentDDValue = 0;
        var tMin = processed['temperatureMin'];

        $.each(processed['temperatureMax'],function(idx,max){
            currentDDValue += (max+tMin[idx])/2;
            ddValues.push(currentDDValue);
        });
        var maxDD = pv.max(ddValues);

        var y1Range = pv.Scale.linear(0,this.getNiceTail(0,maxDD*1.2)).range(0,this.height);
        // Y-Axis
        viz.add(pv.Rule)
                .data(y1Range.ticks())
                .strokeStyle(function(){return (this.index ==0 ? "#000" : "#ddd");})
                .bottom(function(d){return y1Range(d)+bottom_margin;})
                .anchor("right")
                .add(pv.Label)
                .text(function(d){return d.toFixed(0);})
                .anchorTarget().anchor("left")
                .add(pv.Label)
                .text(function(d){return d.toFixed(0);})
        // Chart
        var ddColor = (this.colors[0]);
        ddColor.opacity=0.7;

        var lineDD = viz.add(pv.Area)
                .data(ddValues)
                .bottom(this.bottom_margin)
                .height(y1Range)
                .fillStyle(ddColor)
                .left(function(d){return xRange(dates[this.index]);})
                .add(pv.Dot)
                .title(function(d){return "Degree Days: "+d+" measured on "+Phenonet.Utils.dateFormat.format(dateObj[this.index]);})
                .strokeStyle(null)
                .shapeSize(1) // was .size(1) for 3.2
                .left(function(d){return xRange(dates[this.index]);})
                .bottom(function(d){return y1Range(d)+bottom_margin;})
        viz.add(pv.Bar)
                .bottom(this.bottom_margin)
                .fillStyle("rgba(0,0,0,0.1")
                .data(["Flowering"])
                .left(xRange(dates[75]))
                .height(height*.75)
                .title(" 12/10/2010 - 22/10/2010 (285 to 295)")
                .width(xRange(dates[82])-xRange(dates[75]))
                .anchor("top")
                .top(0)
                .add(pv.Label)

        viz.render();
    }
});

$(function(){
    var map;
    var mapMarkers = [];

    function mapInit(station_names){
        var centerCoord = new google.maps.LatLng(-34.623053999999996, 146.417053);
        var mapOptions = {
            zoom: 17,
            center: centerCoord,
            mapTypeId: google.maps.MapTypeId.HYBRID,
            draggable:true,
            disableDoubleClickZoom:true,
            zoomControl:true,
            maxZoom:17,
            scrollwheel:false
        };
        map = new google.maps.Map(document.getElementById("deployment_map"), mapOptions);

        $.each(station_names, function(idx, item){
            var marker = new google.maps.Marker({
                position: new google.maps.LatLng(item['latitude'], item['longitude']),
                map: map,
                title: idx
            });
            mapMarkers.push(marker);
        });
    }
    $.getJSON( '/get_field_analysis',function(data){
        var processed = Phenonet.Utils.processFieldAnalysisData(data)  ;
        var et0Chart = new plotEt0ToVPD("et0_flot",52,40,40,20,650,200,processed['day'], ["ET0[mm/day]","VPD[kPa]"],["ET0","VPD","Rainfall"],-45,50,0);
        et0Chart.paint(processed);
        var taRaChart = new plotTaToRa( "trh_flot",52,40,40,20,650,200,processed['day'], ["Temperature[Deg C]","Relative Humidity[%]"],["Temperature","Relative Humidity"],-45,100,0);
        taRaChart.paint(processed);
        var ddChart = new plotDegreeDays("dd_flot",52,40,40,20,650,200,processed['day'],  ["Thermal Time[Degree.Days]",""],["Thermal Time"],-45,50,0);
        ddChart.paint(processed);

    });
    $.getJSON( '/get_signals',function(data){
        mapInit(data['Leeton']);
        $(".menu .menu-contents").html(createSignalMenu(data['Leeton']));
    });
});
//Phenonet.updateFieldAnalysis("#et0_flot","#trh_flot","#dd_flot");
function resetError(placeholder,errorMessages){
    $(placeholder+" .error ").html("");
    $(placeholder+" .error ").hide();
    if (errorMessages.length >0){
        $.each(errorMessages,function(idx,err){
            $(placeholder+" .error").append('<li>'+err+'</li>');
            $(placeholder+" .error").show();
        });
    }
}

function createSignalMenu(menu_items){
    var to_return = "";
    $.each(menu_items,function(station_name,elem){
        to_return+='<div class="category">';
        to_return+='<h3>'+station_name+'</h3>';
        to_return+='<ul>';
        $.each(elem['data'],function(sensor,ident){
            to_return+='<li><a ident="'+ident['ident']+'" unit="'+ident['unit']+'" >'+sensor+'</a></li>';
        });
        to_return+='</ul>';
        to_return+='</div>';
    });
    to_return += '<div class="category full-height"  ><h3 id="reset-selection">Reset Selection</h3></div>';
    return to_return;
}

$(function(){
    $(".menu").mouseover(function(){
        $(this).find(".menu-conent-holder").show();
    });
    $(".menu").mouseout(function(){
        $(this).find(".menu-conent-holder").hide();
    });
    $(".menu .category li").live("mouseover",function(){
        $(this).addClass("menu-item-highlight");
    });
    $(".menu .category li").live("mouseout",function(){
        $(this).removeClass("menu-item-highlight");
    });

    $(".menu .category li a").live("click",function(){
        $(this).parents(".menu-conent-holder").hide();
        var ident = $(this).attr('ident');
        var unit = $(this).attr('unit');
        var name = $(this).text();
        var station = $(this).parents(".category").find("h3").text();
        $(this).parents(".menu").find("h3.selection").text(name +" | "+ station);
        $(this).parents(".menu").find("h3.selection").attr("ident",ident);
        $(this).parents(".menu").find("h3.selection").attr("unit",unit);
    });
    $("#reset-selection").live("click",function(){
        $(this).parents(".menu-conent-holder").hide();
        $(this).parents(".menu").find("h3.selection").text(DefaultComparisonSignalLabel);
        $(this).parents(".menu").find("h3.selection").removeAttr("ident");
        $(this).parents(".menu").find("h3.selection").removeAttr("unit");
    });

    $("#apply-btn").click(function(){
        var signals = [];
        var labels = ["#left-label","#right-label"];
        var empty_selection = true;
        $.each(["#signal1","#signal2"],function(idx,d){
            var key = $(d+" h3.selection").attr('ident');
            signals.push(key);
            var lbl = $(d+" h3.selection").text();
            $("#compare "+labels[idx]).text(key==undefined ? "" : lbl);
            if (key != undefined)  {empty_selection = false;}
        });
        if (empty_selection ){
            $(".chart_holder").hide();
            return;
        }
        var sameUnit = $("#signal1 h3.selection").attr('unit') == $("#signal2 h3.selection").attr('unit');
        if (sameUnit){
            $("#left-label").text("")
            $("#right-label").text("")
        }

        $(".chart_holder").show();
        Phenonet.Utils.multiAjax(signals,function(data){
            Phenonet.Utils.plotAccordingToChoices(Phenonet.Utils.getValuesFromKeys(signals,data),"#compare","#flot_compare",sameUnit,Phenonet.Utils.COMPARISON_GRAPH_OPTIONS);
        });
    });
    $("#linear-regression-help").live("click",function(){
        $("#interpolation-description").dialog({width:600,resizable:false});
        return false;
    });
//    plotAccordingToChoices(Phenonet.Utils.getValuesFromKeys(["A","B"],{"A":set1,"B":set2}),"#compare","#flot_compare",["#1f77b4","#ff7f0e"]);


});