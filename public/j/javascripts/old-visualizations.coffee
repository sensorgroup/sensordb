sample = selection_left:  15001 , selection_right: 15001
data:{"day":[15000,15001,15002,15003,15004,15005,15006,15007,15008,15009,15010,15011,15012,15013,15014,15015,15016,15017,15018,15019,15020,15021,15022,15023,15024,15025],"count":[65,76,82,1,39,49,73,12,18,84,56,63,5,100,50,86,59,44,72,6,40,61,94,22,93,68],"min":[0.1151,0.3971,0.4421,0.2941,0.1821,0.6111,0.1211,0.7691,0.1311,0.2611,0.0041,0.7751,0.6111,0.2271,0.1001,0.4931,0.6211,0.2341,0.2291,0.3801,0.5451,0.0691,0.1941,0.4461,0.6121,0.2131],"max":[0.8741,0.7521,0.8141,0.7871,0.8541,0.9561,0.8701,0.9211,0.9951,0.3861,0.6291,0.9491,0.6301,0.4271,0.1261,0.7521,0.8021,0.4671,0.8831,0.4101,0.5631,0.7531,0.5741,0.6271,0.9181,0.6801],"avg":[0.4946,0.5746,0.6281,0.5406,0.5181,0.7836,0.4956,0.8451,0.5631,0.3236,0.3166,0.8621,0.6206,0.3271,0.1131,0.6226,0.7116,0.3506,0.5561,0.3951,0.5541,0.4111,0.3841,0.5366,0.7651,0.4466],"std":[0.1518,0.071,0.0744,0.0986,0.1344,0.069,0.1498,0.0304,0.1728,0.025,0.125,0.0348,0.0038,0.04,0.0052,0.0518,0.0362,0.0466,0.1308,0.006,0.0036,0.1368,0.076,0.0362,0.0612,0.0934]}

Utils  = {}
Utils.DAY_IN_MSEC = 24*60*60*1000
Utils.TIME_ZONE_OFFSET = new Date(1970,0,1,0,0,0).getTime()
Utils.dateFormat = pv.Format.date("%d/%m/%Y")
Utils.convertDayIdxToMSec = (dayIdx) -> dayIdx * Utils.DAY_IN_MSEC+Utils.TIME_ZONE_OFFSET
Utils.createHistogram = (arrayOfData,barCountPerVariety)->
	min = pv.min(arrayOfData)
	max = pv.max(arrayOfData)
	binSize = (max-min)/barCountPerVariety
	totalMap = []
	maxBinSize = -Infinity
	minBinSize = +Infinity
	for value in arrayOfData
		binIdx = if value >= binSize*(barCountPerVariety-1)+min then barCountPerVariety-1 else if value < min+binSize then 0 else parseInt((value-min)/binSize)
		toSet = if totalMap[binIdx] is undefined then 1 else totalMap[binIdx] + 1
		totalMap[binIdx] = toSet
		maxBinSize = toSet if maxBinSize < toSet

	for key,value of totalMap
		minBinSize = value if minBinSize>value

	{binsize:binSize
	min: min
	max:max
	maxY:maxBinSize
	minY:minBinSize
	histogram:totalMap
	}
	
#data:{"day":[14800,15001],"count":[3,84],"min":[0,1],"max":[5,7],"avg":[3,4],"std":[1,0.5]}
# [DayIdx,Min,AVG,Max,STD,Count] # standard error is simply = STD / SQRT(COUNT)
# http://commons.apache.org/math/apidocs/org/apache/commons/math/stat/descriptive/SummaryStatistics.html

class Histogram
	constructor: (@place_holder) ->

	draw: (arrayOfData) ->
		barCountPerVariety = 20
		histogram = Utils.createHistogram(arrayOfData,barCountPerVariety)
		width = 300
		height = 140
		viz = new pv.Panel()
		.canvas(@place_holder)
		.width(width)
		.height(height)
		.left(30)
		.right(30)
		.top(5)
		.bottom(20)
		barWidth = width/barCountPerVariety

		yRange = pv.Scale.linear(histogram['minY']*0.95,histogram['maxY']*1.05).range(0,height)
		xRange = pv.range(histogram['min'],histogram['max'],histogram['binsize'])
		viz.add(pv.Rule)
		.data(x for x in [0..histogram['histogram'].length] by 4)
		.left(->@index*4*barWidth+1)
		.bottom(-15)
		.height(15)
		.anchor("right")
		.add(pv.Label)
		.text((d)->d.toFixed(2))

		viz.add(pv.Panel)
		.data([0..histogram['histogram'].length])
		.add(pv.Bar)
		.left((d)->d*barWidth)
		.width(barWidth)
		.height((d)-> yRange(histogram['histogram'][d]))
		.bottom(001)
		.fillStyle("#1f77b4")
		.title((d)-> d)
		.text((d)-> d == undefined ? 0 : d.toFixed(2) )
		
		viz.add(pv.Rule)
		.data(_.uniq _.map(yRange.ticks(),((num)->num.toFixed(0))))
		.bottom(yRange)
		.strokeStyle("rgba(0,0,0,0.1)")
		.anchor("left")
		.text((d)->d)
		.add(pv.Label)
		.anchorTarget()
		.anchor("right")
		.add(pv.Label)
		.text((d)->d)
		viz.render()
	
class BoxChart
	fromTimestamp = 0
	toTimestamp=  0
	dataSource = undefined
	data = {}
	getData: -> data
	reRangeChartTo: (newfromTimeStamp,newtoTimestamp) -> 
		fromTimestamp = newfromTimeStamp
		toTimestamp = newtoTimestamp
		data = dataSource(fromTimestamp,toTimestamp)
		@viz.render()
	constructor: (@place_holder, @top_margin, @left_margin, @bottom_margin, @right_margin,width, height, ds,fromTs,toTs) ->
		fromTimestamp =fromTs
		toTimestamp=toTs
		dataSource = ds
		xRange = undefined
		yRange = undefined
		data = dataSource(fromTimestamp,toTimestamp)
		GMT_FORMAT = pv.Format.date("%d/%m/%Y")
		TIME_FORMAT = pv.Format.date("%r")
		
		@viz = new pv.Panel().canvas(@place_holder)
		.width(width)
		.height(height)
		.top(@top_margin)
		.left(@left_margin)
		.right(@right_margin)
		.bottom(@bottom_margin)
		.fillStyle("rgba(0,0,0,0.001)")
		@viz.add(pv.Rule).data ->
			diff = (toTimestamp - fromTimestamp)*0.02
			xRange= pv.Scale.linear(fromTimestamp-diff,toTimestamp+diff).range(0,width)
			[new Date(fromTimestamp-diff),new Date(toTimestamp+diff)]
		.strokeStyle("#ddd")
		.height(height)
		.bottom(10)
		.left(-> @index*width)
		.anchor("bottom")
		.add(pv.Label)
		.text((d) -> Utils.dateFormat.format d)

		@viz.add(pv.Rule)
		.data =>
			summaryOfMax = new commons.Summary()
			summaryOfMin = new commons.Summary()
			summaryOfMin.addValue(v) for v in data.min
			summaryOfMax.addValue(v) for v in data.max
			ticks = pv.Scale.linear(summaryOfMin.min,summaryOfMax.max).nice().ticks()
			diff = ticks[1]-ticks[0]
			yRange = pv.Scale.linear(summaryOfMin.min-diff,summaryOfMax.max+diff).range(@bottom_margin,height-@top_margin ).nice()
			yRange.ticks()
		.strokeStyle(-> if @index is 0 then "#000" else "#ddd" )
		.bottom((d)=> yRange(d) )
		.anchor("right")
		.add(pv.Label)
		.text((d) -> d.toFixed(3))
		.anchorTarget().anchor("left")
		.add(pv.Label)
		.text((d) -> d.toFixed(3) )
		# Chart
		# Adding vertical indicator line
		xMouse = undefined
		@viz.add(pv.Rule)
		.visible(-> xMouse isnt undefined )
		.strokeStyle("#eee")
		.left(-> xRange(xMouse))
		.bottom(-4)
		.anchor("bottom").add(pv.Label)
		.text(-> TIME_FORMAT(new Date(xMouse)))
		.anchor("bottom").add(pv.Label)
		.text(-> GMT_FORMAT(new Date(xMouse)))
		# Add a liner chart connecting avg points together
		@viz.add(pv.Panel).add(pv.Line).data(-> [0...data.ts.length])
		.left((idx) -> xRange(data.ts[idx]))
		.bottom((idx) -> yRange(data.avg[idx]))
		.strokeStyle("#000")
		.lineWidth(1)
		#  Add a panel for each data point 
		s = 12
		# alert(xRange(new Date(Utils.convertDayIdxToMSec(data.ts[1])).getTime()))
		points = @viz.add(pv.Panel)
		.data(-> [0...data.ts.length])
		.left((idx) -> xRange(data.ts[idx])-s)
		.width(s * 2)
		
		# Add the range line
		points.add(pv.Rule)
		.strokeStyle(bcolor)
		.left(s)
		.bottom((idx) -> yRange(data.min[idx]) )
		.height((idx) -> yRange(data.max[idx]) - yRange(data.min[idx]))

		# Add the min and max indicators 
		points.add(pv.Rule)
		.strokeStyle(bcolor)
		.data((idx)-> [data.min[idx] , data.max[idx]])
		.bottom((d) -> yRange(d))
		.left(s / 2)
		.width(s)

		# Add the upper/lower quartile ranges
		points.add(pv.Bar)
		.bottom((idx) -> yRange(data.avg[idx] - data.std[idx]) )
		.height((idx) ->   yRange(data.std[idx]) - yRange(-data.std[idx]) )
		.width(s)
		.fillStyle((d) -> "#aec7e8" )
		.strokeStyle(bcolor)
		.lineWidth(1)
		.antialias(false)

		# Add the meadian indicators 
		points.add(pv.Rule)
		.strokeStyle(bcolor)
		.data((idx)-> [data.avg[idx]])
		.bottom((d) -> yRange(d))
		.left(s / 2)
		.width(s)	
		viz = @viz
		@viz.add(pv.Panel)
			.events("all")
			.event "mousemove", ->
				xMouse = xRange.invert(viz.mouse().x)
				viz.render()
			.event "mouseout", -> 
				xMouse = undefined
				viz.render()
				
	colors: pv.Colors.category10().range()	
	bcolor = "#777"


statCalc = (data) ->
	sum = new commons.Summary()
	sum.addValue(v) for v in data
	sum 
	
jQuery.fn.rangify = (options) ->
	leftValue = undefined
	rightValue = undefined
	settings = 
		width:150
		height:25
		top_margin:0
		left_margin:10
		right_margin:10
		bottom_margin:10
		update: ->

	drawRangeComponent = (randomId,settings,histogram_data,left_function,right_function) -> 
		shapeSize = 20
		viz = new pv.Panel()
		.fillStyle("rgba(0,0,0,0.001)")
		.canvas(randomId)
		.width(settings.width)
		.height(settings.height)
		.left(settings.left_margin)
		# .top(settings.top_margin)
		.right(settings.right_margin)
		.bottom(settings.bottom_margin)
		.event("dragend",->settings['update'](leftValue,rightValue))
		height = settings.height
		width = settings.width
		topMargin = settings.top_margin
		data = histogram_data['data']
		base = histogram_data['min']
		period = histogram_data['period']
		selectionIdx = [(histogram_data['selection'][0]-base)/period,(histogram_data['selection'][1]-base+period)/period]
		maxFrequency = pv.max(data)
		yRange= pv.Scale.linear(0,maxFrequency).range(5,settings.height-settings.top_margin*2)
		barWidth = settings.width/data.length 
		functions = [left_function,right_function]
		data2 = $.map selectionIdx, (elem,idx) -> {index:idx, x:elem*barWidth, y:-settings.topMargin+1 , func:functions[idx]}
		# actual histogram
		viz.add(pv.Bar).data(data)
		.width(barWidth-1)
		.height(yRange)
		.bottom(0)
		.left(->@index * barWidth)
		#bars highlighting uncovered regions
		viz.add(pv.Panel).add(pv.Bar)
		.data(data2)
		.fillStyle("rgba(255,255,255,0.7)")
		.left((d) -> d.x*d.index)
		.width((d) -> if d.index is 0 then d.x else width-d.x )
		.height(height)
		 
		# The vertical selection range indicators
		viz.add(pv.Panel)
		.width(width)
		.height(height)
		.event("mousedown", pv.Behavior.drag())
		.event("drag", viz)
		.data(data2)
		.add(pv.Rule)
		.top(-topMargin+1)
		.strokeStyle("#555")
		.lineWidth(1)
		.left (d) ->
			d.x = if d['index'] is 0 then Math.min(data2[1].x+1,d.x) else Math.max(data2[0].x,d.x)
			idx = Math.floor(d.x/barWidth)
			idx = if idx >= data.length then idx = data.length-1 else idx
			d['func'](d.x/barWidth*period+base , idx)
			d.x
		.height(height+topMargin)
		.cursor("move")
		.top(shapeSize/5)
		.add(pv.Dot)
		.fillStyle("#555")
		.strokeStyle(null)
		.shape("triangle")
		.add(pv.Dot)
		.angle(->Math.PI) 
		.shape("triangle")
		.top(height+topMargin+1)
		viz.render()
		
	@.each ->
		if options then $.extend( settings, options )
		$this = $(this)
		data = $this.data('histogram')
		if !data 
			alert("No Data Set for Histogram.")
			return
		
		$this.append("<input type='text' class='range-text left float-left' readonly='' />")
		randomId = "rand_"+parseInt(Math.random()*100000)
		$this.append("<div id='"+randomId+"'class='range-histogram float-left' ></div>")
		$this.append("<input type='text' class='range-text right float-left'  readonly='' />")
		leftFunction = (value,idx) ->	
			leftValue= Utils.convertDayIdxToMSec(value)
			date = Utils.dateFormat.format(new Date(leftValue))
			$this.find('input.range-text').first().val(date)
			$this.data 'min-val',value
		rightFunction = (value,idx) ->
			rightValue = Utils.convertDayIdxToMSec(value)
			date = Utils.dateFormat.format(new Date(rightValue))
			$this.find('input.range-text').last().val(date)
			$this.data('max-val',value)
		drawRangeComponent(randomId, settings,data,leftFunction,rightFunction)
$ ->
	generator = (from,to)->
		result = {ts:[], count:[],min:[],max:[],avg:[],std:[]}
		diff = to-from
		if (diff < 0 ) then throw "From timestamp should be before To timestamp"
		count = 30 
		if diff is 0 then return result
		for i in [from..to] by diff/count
			result.count.push parseInt(Math.random().toFixed(2)*100)
			min = parseFloat Math.random().toFixed(3)+1
			max = parseFloat Math.random().toFixed(3)+1
			if (min > max)
				[min,max] = [max,min]
			result.max.push max
			result.min.push min
			result.avg.push parseFloat(((min+max)/2).toFixed(4))
			result.std.push parseFloat(((max-min)/5).toFixed(5))
			result.ts.push i
		result
	
	$("#range").data('histogram', min: sample.data.day[0],
	period:0.999999
	data: sample.data.avg
	selection:[sample.selection_left,sample.selection_right]
	)
	
	boxChart = new BoxChart("box_chart",5,40,20,40,650,200,generator,Utils.convertDayIdxToMSec(sample.selection_left),Utils.convertDayIdxToMSec(sample.selection_right+1))

	filterUpdateFinished= (leftIdx,rightIdx)->
		boxChart.reRangeChartTo(leftIdx,rightIdx)
		# alert(boxChart.getData().ts.length)

	$(".rangify").rangify { width:318, 
	height:35,
	top_margin:5,
	left_margin:10,
	right_margin:10,
	bottom_margin:10,
	update:filterUpdateFinished }
	
	boxChart.reRangeChartTo(new Date().getTime()-90*24*60*60*1000,new Date().getTime())
	
	histogram = new Histogram("histogram_1")
	histogram.draw([1,2,2,2,5,6,7,8,9,10])

