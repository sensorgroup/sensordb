Date.prototype.nextMonth = function () {
    var to_return = this;
    if (this.getMonth() == 11)
        to_return = new Date(this.getFullYear() + 1, 0, 1,0,0,0,0);
    else
        to_return  =  new Date(this.getFullYear(), this.getMonth() + 1,1,0,0,0,0);
    return to_return;
};
Date.prototype.monthRange = function(toDate){
    var to_return = [];
    var start = this.nextMonth();
    while (start.getTime() < toDate.getTime()){
        to_return.push(start);
        start = start.nextMonth();
    }
    return to_return;
}
/**
 * ZipWithIndex receives [A,B,C] and produces [[0,A],[1,B],[2,C]]
 */
Array.prototype.zipWithIndex = function(){
    var to_return = [];
    for (var idx = 0;idx<this.length;idx++) {
      to_return.push([idx,this[idx]]);
    }
    return to_return;
};
/**
 * ZipWithRightCell receives [A,B,C] and produces [[A,Func(0)],[B,Func(1)],[C,Func(2)]]
 */
Array.prototype.zipRightWithFunc = function(rightCellFunc){
    var to_return = [];
    for (var idx = 0;idx<this.length;idx++) {
        to_return.push([this[idx],rightCellFunc(idx)]);
    }
    return to_return;
};

/**
 * ZipWithLeftCell receives [A,B,C] and produces [[Func(0),A],[Func(1),B],[Func(2),C]]
 */
Array.prototype.zipLeftWithFunc = function(leftCellFunc){
    var to_return = [];
    for (var idx = 0;idx<this.length;idx++) {
        to_return.push([leftCellFunc(idx),this[idx]]);
    }
    return to_return;
};


var Phenonet = {
    updateFieldAnalysis : function(et0PlaceHolder,thrPlaceHolder,ddPlaceHolder){
        $.ajax({
            url:"/get_field_analysis",
            dataType:"json",
            success:function(data){

            }

        });
    }
};

Phenonet.Utils = {
    BASE_DATE: 0,
    sortKeysInObject: function(someObject){
        var keys = [];
        $.each(someObject,function(idx,value){
            keys.push(idx);
        });
        keys.sort();
        var to_return = {};
        $.each(keys,function(idx,value){
            to_return[value] = someObject[value];
        });
        return to_return;
    },
    dayIdxToSec : function(dayIdx){
        return (this.BASE_DATE+ dayIdx*24*60*60);
    },
    getKeys : function (dataset){
        var toReturn = [];
        if (dataset.constructor == Array){
            for (var idx = 0;idx<dataset.length;idx++) {
                toReturn.push(idx);
            }
        } else {
            for (var name in dataset) {
                toReturn.push(name);
            }
        }
        return toReturn;
    },
    getNiceMinMax:function(min,max){
        var newMin = pv.Scale.linear(min,max).nice().ticks()[0]
        var newMax = pv.Scale.linear(min,max).nice().ticks().pop()
        return [newMin,newMax];
    },
    processFieldAnalysisData : function(data){
        var taMax = [];
        var taMin = [];
        var taEt0 = [];
        var taVpd = [];
        var taRh = [];
        var taRain = [];
        var day = [];
        for (var idx = 0;idx< data.length;idx++){
            var item = data[idx];
            var date = (Phenonet.Utils.dayIdxToSec(data[idx]['day']));
            taMax.push(item['tMax']);
            taMin.push(item['tMin']);
            taEt0.push(item['et0']);
            taVpd.push(item['vpd']);
            taRh.push(item['rh']);
            taRain.push(item['rain']);
            day.push(date);

        }
        return {
            temperatureMax:taMax,
            temperatureMin:taMin,
            et0:taEt0,
            rh:taRh,
            vpd:taVpd,
            rain:taRain,
            day:day
        };
    }
};

Phenonet.comapre_chart_colors = ["#1F77B4","#FFA759"];

Phenonet.Utils.convertTimeStampInMSec = function(epochTime,timeZoneOffset){
    var some_date = new Date();
    some_date.setTime(epochTime);
    var curr_date = some_date .getDate();
    var curr_month = some_date .getMonth();
    var curr_year = some_date .getFullYear();
//        var curr_hour = some_date .getHours();
//        var curr_min = some_date .getMinutes();
//        return curr_hour+":"+curr_min+" "+curr_date+"/"+(curr_month+1)+"/"+curr_year ;
    return curr_date+"/"+(curr_month+1)+"/"+curr_year ;
}
Phenonet.Utils.getValuesFromKeys = function(keysArray,obj){
    var to_return = [];
    for (var key in keysArray) {
        to_return.push(obj[keysArray[key]]);
    }
    return to_return;
}

Phenonet.Utils.multiAjax = function(arrayOfIdents,callbackFunc){
    function ajaxDone(req_list,func){
        for (var status in req_list){
            if (req_list[status] == "waiting")
                return;
        }
        func(req_list);
    }
    var ajax_requests = {};
    $.each(arrayOfIdents,function(sigIdx,ident){
        if (ident != undefined){
            ajax_requests[ident]= "waiting";
            $.ajax({
                url: "get_data",
                data: {'ident':ident},
                cache:true,
                dataType:'json',
                success: function(fromSrc){
                    if(fromSrc == null || fromSrc ==  undefined || fromSrc.length ==0)
                    {ajax_requests[ident] = null;}
                    else {
                        var processed = $.map(fromSrc,function(item){
                            return new Array([parseInt(item[0])*1000,item[1]]);
                        });
                    }
                    ajax_requests[ident] = processed;
                    ajaxDone(ajax_requests,callbackFunc);
                },
                failure: function(fromSrc){
                    alert("Error: "+fromSrc);
                    ajax_requests[ajax_key] = undefined;
                    ajaxDone(ajax_requests,callbackFunc);
                }
            });
        }
    });
}

Phenonet.Utils.dateFormat = pv.Format.date("%d/%m/%Y");
Phenonet.Utils.parseDateToDayIdx = function(dateInDMYFormat){
    // Example input "31/03/2011"
    return parseInt(Math.ceil(Phenonet.Utils.dateFormat.parse(dateInDMYFormat)/(1000*60*60*24))) ; // round up !
}
Phenonet.Utils.parseTimeToDayMinute = function(timeInMHFormat){
    // Example input "02:21"
    return parseInt(timeInMHFormat.split(":")[0])*60 + parseInt(timeInMHFormat.split(":")[1]);
}
Phenonet.Utils.dateTimeFormat = function (date){
    return date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+" "+date.getDate()+"/"+(date.getMonth()+1)+"/"+date.getFullYear();
}

Phenonet.Utils.COMPARISON_GRAPH_OPTIONS = {
    legend: { position: 'nw' ,backgroundOpacity:0.5, backgroundColor: null},
    series: {
        lines: { lineWidth:1 }
    },
    xaxis: { mode: 'time', localTimezone: true  },
    yaxes: [{position:"left"
    },{position:"right"}],
    selection: { mode: "xy" },
    grid: {
        show: true,
        borderWidth:1,
        borderColor:"#ccc"
    }
}

Phenonet.Utils.SCATTERED_TIME_SERIES_GRAPH_OPTIONS = {
    legend: { position: 'nw' ,backgroundOpacity:0.5, backgroundColor: null},
    series: {
        lines: { show: false },
        points: { show: true,radius: 1 }
    },
    xaxis: { mode: 'time', localTimezone: true  },
    yaxes: [{position:"left"
    },{position:"right"}],
    selection: { mode: "xy" },
    grid: {
        show: true,
        borderWidth:1,
        borderColor:"#333",
        hoverable: true, //for tooltip
        clickable: true  //for tooltip
    }
}
Phenonet.Utils.SCATTERED_GRAPH_OPTIONS = {
    legend: { position: 'nw' ,backgroundOpacity:0.5, backgroundColor: null},
    series: {
        lines: { show: false },
        points: { show: true,radius: 1 }
    },
    xaxis: {  },
    yaxes: [{position:"left"
    },{position:"right"}],
    selection: { mode: "xy" },
    grid: {
        show: true,
        borderWidth:1,
        borderColor:"#333",
        hoverable: true, //for tooltip
        clickable: true  //for tooltip
    }
}


function showTooltip(x, y, contents) {
    $('<div id="tooltip">' + contents + '</div>').css( {
        position: 'absolute',
        display: 'none',
        top: y + 7,
        left: x + 12,
        border: '1px solid #ddd',
        padding: '2px',
        'background-color': '#eee',
        opacity: 0.80
    }).appendTo("body").fadeIn(200);
}

Phenonet.Utils.plotAccordingToChoices=function(rawInputData,placeHolder,flotHolder,singleAxis,ops,scattered_color) {
    //    var errorMsgs = [];
    var reformedData = [];
    var isTimeSeries = (ops == Phenonet.Utils.SCATTERED_TIME_SERIES_GRAPH_OPTIONS || ops == Phenonet.Utils.COMPARISON_GRAPH_OPTIONS);
    var isLineShown = (ops == Phenonet.Utils.COMPARISON_GRAPH_OPTIONS);

    $.each(rawInputData.constructor == Object ? rawInputData['data']:rawInputData,function(idx,e){
        var color = scattered_color ? scattered_color[idx]: Phenonet.comapre_chart_colors[idx];
        if (e != undefined){
            reformedData.push({
                data:e,
                yaxis: singleAxis ? 1: idx+1,
                color:color,
                shadowSize:0,
                lines: { show: isLineShown}
            });
        }
        if (rawInputData.constructor == Object  &&
                rawInputData['line'] != undefined &&
                rawInputData['line'][idx] !=undefined){

            reformedData.push({
                data:rawInputData['line'][idx],
                yaxis: singleAxis ? 1: idx+1,
                color:color,
                shadowSize:0,
                lines: { show: true } // line for trend
            });
        }
    });

//    resetError(placeHolder,errorMsgs);

    var flot = $.plot($(flotHolder), reformedData,  ops);
    var start_date = Phenonet.Utils.convertTimeStampInMSec(flot.getAxes()['xaxis']['min']);
    var end_date = Phenonet.Utils.convertTimeStampInMSec(flot.getAxes()['xaxis']['max']);
    $(placeHolder+ " .selection_range").html('Plot for <span class="date">'+ start_date+ '</span> until <span class="date">' + end_date+'</span>');
    $(flotHolder).unbind("plotselected").bind("plotselected", function (event, ranges) {
        var start_date = Phenonet.Utils.convertTimeStampInMSec(ranges.xaxis.from.toFixed(1));
        var end_date = Phenonet.Utils.convertTimeStampInMSec(ranges.xaxis.to.toFixed(1));
        if (start_date != end_date)
            $(placeHolder+" .selection_range").html('Plot for <span class="date">'+ start_date+ '</span> until <span class="date">' + end_date+'</span>');
        else
            $(placeHolder+" .selection_range").html("<div class='prepend-1'>Plot for <span class='date'>"+start_date+ "</span></div>");

        plot_zoomed = $.plot($(flotHolder), reformedData,
                $.extend(true, {}, ops, {
                    xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to },
                    yaxis: { min: (ranges.yaxis == undefined ? 0 : ranges.yaxis.from), max: (ranges.yaxis == undefined ? 0 : ranges.yaxis.to) },
                    y2axis: { min: (ranges.y2axis == undefined ? 0 : ranges.y2axis.from), max: (ranges.y2axis == undefined ? 0 : ranges.y2axis.to) }
                }));
        $(placeHolder+" .reset-btn").unbind("click").click(function(){
            Phenonet.Utils.plotAccordingToChoices(rawInputData,placeHolder,flotHolder,singleAxis,ops,scattered_color);
        });
    });
    var previousPoint = null;
    $(flotHolder).bind("plothover", function (event, pos, item) {
        if (item) {
            if (previousPoint != item.datapoint) {
                previousPoint = item.datapoint;
                $("#tooltip").remove();
                var x = item.datapoint[0],
                        y = item.datapoint[1].toFixed(2);
                if (isTimeSeries){
                    showTooltip(item.pageX, item.pageY, y+" at " +$.plot.formatDate(new Date(x),"%h:%M:%S %d/%m/%y"));
                } else {
                    showTooltip(item.pageX, item.pageY, "("+x+","+y+")");
                }
            }
        }
        else {
            $("#tooltip").remove();
            previousPoint = null;
        }
    });

}


//todo: it looks like I only use min and max for date object
Phenonet.PhenonetChart = Class.extend({
    init: function(place_holder,top_margin,left_margin,right_margin,bottom_margin,width,height,dates,axis_names,legend,legend_top_margin,legand_size,legend_left_extra_margin) {
        this.place_holder = place_holder;
        this.dates = dates;
        this.axis_names = axis_names;
        this.legend = legend;
        this.left_margin = left_margin;
        this.top_margin = top_margin;
        this.bottom_margin = bottom_margin;
        this.right_margin = right_margin;
        this.width = width;
        this.height = height;
        this.maxDate = pv.max(dates);
        this.dateObj = dates.map(function(d){return new Date(d*1000);});
        var dateObj = this.dateObj;
        this.minDate = pv.min(dates);
        monthNameFormat = pv.Format.date("%b");
        tempDateFormat= pv.Format.date("%Y-%m");
        this.xRange= pv.Scale.linear(pv.min(this.dates),pv.max(this.dates)).range(0,this.width);
        var xRange = this.xRange;
        this.viz = new pv.Panel()
                .canvas(place_holder)
                .width(this.width)
                .height(this.height)
                .top(this.top_margin)
                .left(this.left_margin)
                .right(this.right_margin)
                .bottom(this.bottom_margin)
                .fillStyle("rgba(0,0,0,0.001)");

        this.viz.add(pv.Rule).data([this.dates[0],this.dates[this.dates.length-1]])
                .strokeStyle("#ddd")
                .height(height+bottom_margin)
                .bottom(0)
                .left(this.xRange)
                .anchor("bottom")
                .add(pv.Label)
                .text(function(d){return Phenonet.Utils.dateFormat.format(new Date(d*1000));});

        var monthIndicators = dateObj[0].monthRange(dateObj[dateObj.length-1]);

        this.legand_size = legand_size;
        this.legend_top = legend_top_margin;
        var legend_dot_size = 15;
        var colors = this.colors;

        this.viz.add(pv.Rule)
                .data(monthIndicators)
                .strokeStyle("#ddd")
                .height(this.height+this.bottom_margin/3)
                .left(function(d){return xRange(d.getTime()/1000);})
                .bottom(2*this.bottom_margin/3)
                .anchor("bottom")
                .add(pv.Label)
                .textAlign("left")
                .text(function(d){return monthNameFormat.format(d); })
                .font("bold 11px arial")

        //places the legends.
        var legend_left_offset = (this.width-legand_size*legend.length)/2 + legend_left_extra_margin;
        this.viz.add(pv.Dot)
                .data(this.legend)
                .top(this.legend_top)
                .shapeSize(legend_dot_size) // was .size(legend_dot_size) for 3.2
                .left(function(){return legend_left_offset+legand_size*this.index;})

                .strokeStyle(null)
                .fillStyle(function(){return colors[this.index];})
                .add(pv.Label)
                .left(function(){return legend_left_offset+legand_size*this.index + legend_dot_size/2;})
                .textBaseline("middle")

        // places the axis names
        this.viz.add(pv.Label)
                .data(this.axis_names)
                .top(legend_top_margin)
                .left(function(d){return axis_names.length == 2 ? [d.length*2,width-d.length*2][this.index] : width/2 ; })
                .font("bold 12px arial")
                .textAlign("center")
                .textBaseline("middle")
                .fillStyle(function(){return colors[this.index];})
    },
    colors:pv.Colors.category10().range(),
    getNiceTail:function (start,end){
        var range = pv.Scale.linear(start,end).ticks();
        return range[range.length-1];
    }
});
