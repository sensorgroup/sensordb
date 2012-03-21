@module "range_helper" , ->
	class @CalendarUnit
		# file:///Users/ali/java/protovis-3.3.1/jsdoc/symbols/pv.Format.date.html
		constructor: (@from,@to) ->
		getStart: -> new Date(@from.getTime() - @offset(@from).getTime())
		getEnd: -> new Date(@to.getTime() - @offset(@to).getTime())
		niceStep: ->
			range = @getEnd() - @getStart()
			alert(@PERIOD)
			if (range / @PERIOD) > 7
				period = @PERIOD
				while (range / period) > 7
					period*=2
				period
			else if (range /@PERIOD) <= 3
				period = @SUB_PERIOD
				while range / period > 7
					period*=2
				period
			else
				@PERIOD
	
	class @RangeCalculator
		LABEL_PER_AXIS = 7
		YEAR =  period: 365*24*60*60*1000 , 
		format : pv.Format.date("%Y") , 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),0,0,0,0,0,0)
		MONTH = period: 31*24*60*60*1000 , 
		format: pv.Format.date("%B"), 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),dateObj.getMonth(),0,0,0,0,0)
		DAY = period: 24*60*60*1000 , 
		format: pv.Format.date("%A"), 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),dateObj.getMonth(),dateObj.getDate(),0,0,0,0)
		HOUR = period: 60*60*1000 , 
		format:  pv.Format.date("%I %p"), 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),dateObj.getMonth(),dateObj.getDate(),dateObj.getHours(),0,0,0) 
		MINUTE = period: 60*1000 , 
		format: pv.Format.date("%M"), 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),dateObj.getMonth(),dateObj.getDate(),dateObj.getHours(),dateObj.getMinutes(), 0,0)
		SECOND = period: 1000 , 
		format: pv.Format.date("%S"), 
		offset: (dateObj)-> new Date(dateObj.getFullYear(),dateObj.getMonth(),dateObj.getDate(),dateObj.getHours(),dateObj.getMinutes(), dateObj.getSeconds(),0)
			
		options = [YEAR,MONTH,DAY,HOUR,MINUTE,SECOND]
		constructor: (@from,@to) ->
			period = @to.getTime() - @from.getTime()
			@subAxis = []
			@borderAxis = (_.detect options, (item)=> period> item.PERIOD ) ? SECOND
			if (period / @borderAxis.period >= 3) 
				for timestamp in [@to.getTime .. @from.getTime()] by @borderAxis.period 
					if (timestamp is @to or timestamp is @from)
						@subAxis.push(@borderAxis.format(timestamp))
					else
						@subAxis.push(@borderAxis.format(@borderAxis.offset(timestamp)))
			else 
				throw "Not implemented Yet !"
		leftAxisBorder = -> @borderAxis.format(@from)
		rightAxisBorder = -> @borderAxis.format(@to)
