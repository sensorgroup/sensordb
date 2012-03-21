window.module = (name, fn)->
  if not @[name]?
    this[name] = {}
  if not @[name].module?
    @[name].module = window.module
  fn.apply(this[name], [])

Date::nextMonth = () ->
	if (@getMonth() == 11)
		new Date(@getFullYear() + 1, 0, 1,0,0,0,0)
	else
		 new Date(@getFullYear(), @getMonth() + 1,1,0,0,0,0)

Date::monthRange = (toDate) ->
	to_return = []
	start = @nextMonth()
	while (start.getTime() < toDate.getTime())
		to_return.push(start)
		start = start.nextMonth()
	to_return
@module "commons" , ->
	class @Summary
		constructor: ->
			@count=0
			@min = undefined
			@max = undefined
			@mEst =0
			@M2 = 0
			@sum = 0
		
		addValue: (value)->
			value = parseFloat(value)
			@count+=1
			if @min is undefined or @min >value then @min = value	
			if @max is undefined or @max <value then @max = value
			delta = value - @mEst	
			@mEst +=  delta / @count
			@M2 +=	delta * (value-@mEst)
			@sum +=value
		
		variance: -> if @count ==1 then 0 else if @count is undefined then undefined else @M2 / (@count-1)
		std: -> if @count is 0 then undefined else if @count is 1 then 0 else Math.sqrt(@variance())
		average: -> @sum/@count
