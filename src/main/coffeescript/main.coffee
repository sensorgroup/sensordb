@module "sensordb", ->
	class @LineChart
		constructor: (@location,@unit,@data_provider_func) ->
			@elem = $(location)
			options =
				legend:
					position: 'nw'
					backgroundOpacity:0.5
					backgroundColor: null
				series:
					lines:
						lineWidth: 1
				xaxis:
					mode: 'time'
					localTimezone: false
				yaxes: [{position:"left",axisLabel: unit,axisLabelUseCanvas: true}]
				grid:
					show: true
					borderWidth:1
					borderColor:"#ccc"
				selection:{mode: "xy"}

#			to_plot = _.map req_res, (rr)->
#				shadowSize:1
#				data: rr._res.data
#				yaxis: (if rr.local_info.axis is 'left' then 1 else 2)
#			place_holder = @elem.find('.flot')
#			place_holder.unbind("plotselected").bind "plotselected", (event,ranges)=>
#				plot = $.plot(place_holder,to_plot,($.extend(true, {}, options,
#					xaxis:
#						min: ranges.xaxis.from
#						max: ranges.xaxis.to,
#						yaxis:
#							min: (if ranges.yaxis is undefined then 0 else ranges.yaxis.from)
#							max: (if ranges.yaxis is undefined then 0 else ranges.yaxis.to)
#					y2axis:
#						min: (if ranges.y2axis is undefined then 0 else ranges.y2axis.from)
#						max: (if ranges.y2axis is undefined then 0 else ranges.y2axis.to)
#						axisLabel: (if right_axis_unit.length>0 then right_axis_unit[0] else undefined)
#				)))
#				@elem.find(".caption .reset-zoom").unbind("click").click => @plot(conf,req_res)
#				start_date = parseInt((plot.getAxes()['xaxis']['min']).toFixed(0))
#				end_date = parseInt((plot.getAxes()['xaxis']['max']).toFixed(0))
#				# Verify the TimeZone and perform correct formatting of the timestamp
#				@elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format())
#				@elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format())
#
#			plot = $.plot(place_holder, to_plot, options)
#			start_date = plot.getAxes()['xaxis']['min']
#			end_date = plot.getAxes()['xaxis']['max']
#
#			@elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format())
#			@elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format())
#			@elem.find(".caption").show()


		@guid= ->
			S4 = -> (((1+Math.random())*0x10000)|0).toString(16).substring(1)
			(S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4())
		@find_in_catalog_by_stream_id =  (stream_id,catalog) ->
			for user,experiments of catalog
				for exp_name,nodes of experiments
					for node_name, streams of nodes
						for stream_name,s_id of streams
							if (s_id.sid == stream_id)
								return [user,exp_name,node_name,stream_name,s_id.unit]
			return undefined
	class @DataRequest
		constructor: (@local_info,@user_id,@experiment,@node,@stream,@fields,@checkCache,@live,@from,@to) ->
			@id = [@user_id,@experiment,@node,@stream,_.map(@fields,(v,k)->"#{k}->#{v}"),@checkCache,@live,@from,@to].join('$')

	class @GroupedDataRequest
	# The call_back is called using @data requests_array which contains DataRequest objects along with _res attribtue containing responses.
	#
		constructor: (@local_info,@data_requests,@call_back)->
			_.each(@data_requests,(r)->r._res=undefined)
			@id = _.map(@data_requests,(req)->req.id).join("$$")
			@live = _.any(@data_requests,(r)-> r.live)

		fire: -> @call_back(@data_requests)

	class @GroupedRequestManager
		constructor: (@db) ->
			@listeners = {}
		add_grouped_data_request: (grouped_data_request) ->
			if not @listeners[grouped_data_request.id]
				@listeners[grouped_data_request.id]=grouped_data_request
				_.each grouped_data_request.data_requests , (req) =>
					@db.download req, (res) => @data_received(req.id,res)
			else
				console.log("This request #{grouped_data_request.id} exists in the listener's list.")
		remove_grouped_data_request: (grouped_data_request) => delete @listeners[grouped_data_request.id]
		data_received: (data_request_id,data_response) ->
			_.each @listeners,(grouped_data_request) =>
				anything_to_set=_.filter grouped_data_request.data_requests, (req)=>
					req.id is data_request_id and (not req._res or (req._res and req.live))
				if anything_to_set.length>0
					_.each(anything_to_set,(r)-> r._res=data_response)
					if _.all(grouped_data_request.data_requests,(req)-> req._res != undefined)
						grouped_data_request.fire()
						if (not grouped_data_request.live)
							delete @listeners[grouped_data_request.id]
							console.debug("Grouped Data Request #{grouped_data_request.id} is triggered and removed (not live).")

	class @Database
		analysis: (username) ->
			#  callback parameter is called with one parameter, a json object returned from server (not modified).
		download: (data_request,callback) ->
			# User pub, sub to download data.
		catalog: (selectors) ->

	class @Widget extends Backbone.Events # use modules to define namespaces and extend across them
		constructor: (@widget)->
			@db = window.db
			@elem = $("\##{@widget.html_id}")
			@id = @widget.html_id
			@process_config(@widget.conf)
		dataFeeds: (conf)-> alert('not implemented')
		process_config: (new_conf)->

		sample_config: -> alert('not implemented')

class WidgetStore
	constructor: ->
		@widgets={}
		@active_widgets=[] # this should be a backend.js collection
	registerWidget: (widget_name,widget) -> @widgets[widget_name]=widget
	getWidgetFor: (widget_name) -> @widgets[widget_name]
	handler2ClassName: (widget_name) -> _('-'+widget_name).camelize()
	className2Handler: (className) -> _(className).underscored()
	addWidgetInstnace: (widget_instance) ->
		@active_widgets.push(widget_instance)

window.ws = new WidgetStore()


class SampleDatabase extends sensordb.Database
	analysis: (username) ->
		'analysis1':
			'Temperature Range and Humidity Widget':
				handler:'WidgetTemperatureHumidityRange'
				description: "Use this widget to plot raw sensor data. You can configure individual axies using the following menu."
				conf:
					width: '100%'
					height: '250px'
					zoom: false
					line_shadow:0
					allow_axis_selection:true
					from_date: '10/10/2004'
					to_date:  ''
					feeds: ['ali/Yanco','ali/Yanco2']
					live: false
					initial_graph:
						left_axis:[{
						user:'ali'
						experiment:'Yanco1'
						node:'Janz [Irrigated]'
						stream:'Canopy Temperature'
						from_timestamp: 1331439787953
						to_timestamp: undefined
						fields:
							data:1
						},{
						user:'ali'
						experiment:'Yanco2'
						node:'Janz [Irrigated]'
						stream:'Canopy Temperature'
						from_timestamp: 1331439787953
						to_timestamp: undefined
						fields:
							data:1
						}]
						right_axis:[{
						user:'ali'
						experiment:'Yanco1'
						node:'Janz [Irrigated]'
						stream:'Canopy Temperature'
						from_timestamp: 1331439787953
						to_timestamp: undefined
						fields:
							data:1
						}]
			'Degree Days Widget':
				handler:'WidgetEt0Vpd'
				conf:
					from_date: '10/10/2004'
					to_date:  ''
		'analysis2':{}

	catalog : (selectors) ->
		'ali':
			'Yanco1':
				'Janz [Irrigated]':
					'Canopy Temperature':
						sid:'1'
						unit:'Temperature (C)'
					'Body Temperature' :
						sid:'2'
						unit:'Light'
				'Weather':
					'PAR':
						sid:'3'
						unit:'Humidity (H)'
					'Wind Speed (Max)':
						sid:'4'
						unit:'Temperature (C)'
					'Wind Speed (Average)':
						sid:'5'
						unit:'Temperature (C)'
					'Air Pressure':
						sid:'6'
						unit:'Temperature (C)'
					'Air Pressure (Sea Level)':
						sid:'7'
						unit:'Temperature (C)'
					'Relative Humidity':
						sid:'8'
						unit:'Temperature (C)'
					'Wind Speed (Min)':
						sid:'9'
						unit:'Temperature (C)'
					'Solar Flux':
						sid:'10'
						unit:'Temperature (C)'
					'Air Temperature':
						sid:'11'
						unit:'Temperature (C)'
					'Rain Accumulation':
						sid:'12'
						unit:'Temperature (C)'
			'Yanco2':
				'Janz [Irrigated]':
					'Canopy Temperature':
						sid:'13'
						unit:'Temperature (C)'
					'Body Temperature' :
						sid:'14'
						unit:'Temperature (C)'
					'Weather':
						sid:'15'
						unit:'Temperature (C)'
					'Wind Speed (Max)':
						sid:'16'
						unit:'Temperature (C)'
					'Wind Speed (Average)':
						sid:'17'
						unit:'Temperature (C)'
					'Air Pressure':
						sid:'18'
						unit:'Temperature (C)'
					'Air Pressure (Sea Level)':
						sid:'19'
						unit:'Temperature (C)'
					'Relative Humidity':
						sid:'20'
						unit:'Temperature (C)'
					'Wind Speed (Min)':
						sid:'21'
						unit:'Temperature (C)'
					'Solar Flux':
						sid:'22'
						unit:'Temperature (C)'
					'Air Temperature':
						sid:'23'
						unit:'Temperature (C)'
					'Rain Accumulation':
						sid:'24'
						unit:'Temperature (C)'
	download: (data_request,callback) ->
		start_time = (new Date()).getTime()
		sample =
			label:"Sample Data #{Math.random()*100}"
			data: []
			id : data_request.id
		_.times(Math.random()*100+100, (i) -> sample.data.push([start_time+i*100,Math.random()*10+i]))
		callback(sample)

window.db = new SampleDatabase()
window.rm = new sensordb.GroupedRequestManager(window.db)

	layout:(template_id,template_params={},callback_func=(session)->{})->
		@session (session)->
			session ||={}
			$("body div#navigation").html((_.template($("#navbar-tpl").html(),{session:session})))
			if template_id isnt first_page_tpl
				$("body div#header").html(_.template($("#header-tpl").html()))
			else
				$("body div#header").html("")
			$("body div#contents").html(_.template($(template_id).html(),template_params))
			if template_id isnt first_page_tpl
				$("body div#footer").html(_.template($("#footer-tpl").html()))
			else
				$("body div#footer").html("")
			sensordb.Utils.scroll_top()
			callback_func(session)

	register: ->
		@layout "#tpl-register",{},()->
			$("body textarea").cleditor(sensordb.Utils.editor_config)
			$("#registration a.btn-primary").click ->
				$.ajax({type:'post', url:'/register', data:$("#registration").serialize(),success:((res)->console.log(res)),error:sensordb.show_errors})

	parallel_requests: (requests,callback)->
		done = _.size(requests) # Number of total requests
		to_return = {}
		if (done==0) then callback(to_return)
		_.each requests, (req,name) ->
			$.ajax(_.extend(req,{success:(data)->
				to_return[name]=data
				done -=1
				callback(to_return) if(done ==0)
			}))

	data_page: (username) ->
		$.ajax type:"get", url:"/session" , data:{user:username},dataType:"json" , success:(profile)=>
			@layout "#tpl-data-page",{profile},()->
				$("table.tablesorter").tablesorter()

	analysis: (user,name) ->
		widgets = db.analysis(user)[name]
		_.each widgets,(value) ->
			value.html_id=sensordb.Utils.guid()
			$LAB.script("/j/#{value.handler}.js").wait ->
				if (window.ws.getWidgetFor(value.handler))
					window.ws.addWidgetInstnace(new (window.ws.getWidgetFor(value.handler))(value))
				else
					alert("Failed! Loading widget #{value.handler}")

		@layout("#tpl-analysis", {widgets})
