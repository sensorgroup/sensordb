class WidgetTemperatureHumidityRange extends sensordb.Widget
	constructor: (e)->
		super(e)
	sample_config: -> """
		Sample configuration goes here.
	"""
	html_template_name: -> "#tpl-flot-chart"
	html_template_config_name: ->"#tpl-flot-chart-config"
	
	# Processes this widget based on parameters provided in the config parameter
	# c : a config object
	process_config: (c)->
		initial_selection=_.union(c.initial_graph.left_axis,c.initial_graph.right_axis)
		all_feeds = _.union(c.feeds,_.map(initial_selection,(x)->"#{x.user}/#{x.experiment}/#{x.node}/#{x.stream}"))
		@catalog = catalog= window.db.catalog(all_feeds)
		if c.allow_axis_selection
			@elem.find(".in-widget-config").html(_.template($(@html_template_config_name()).html(),{@id,@catalog,left_axis:c.initial_graph.left_axis,right_axis:c.initial_graph.right_axis}))
		
			@elem.find(".in-widget-config .modal input:checkbox").bind 'change', =>
				left_selection = []
				@elem.find(".in-widget-config .Left.modal input:checked").each -> 
					sid = $(@).attr('feed-id')
					[user,experiment,node,stream,unit] = sensordb.Utils.find_in_catalog_by_stream_id(sid,catalog)
					field = {user,live:c.live,node,sid,stream,experiment,unit,to_date:c.to_date,from_date:c.from_date}
					left_selection.push(field)
				right_selection = []
				@elem.find(".in-widget-config .Right.modal input:checked").each -> 
					sid = $(@).attr('feed-id')
					[user,experiment,node,stream,unit] = sensordb.Utils.find_in_catalog_by_stream_id(sid,catalog)
					field = {user,live:c.live,node,sid,stream,experiment,unit,to_date:c.to_date,from_date:c.from_date}
					right_selection.push(field)
				
				_.each {Left:left_selection,Right:right_selection}, (selection,direction)->
					if (selection.length==0)
						$(".in-widget-config .#{direction}.modal input:checkbox").removeAttr("disabled")
					else
						$(".in-widget-config .#{direction}.modal input:checkbox[unit!='#{selection[0].unit}']").attr('disabled', true)
				
				@prepare_and_plot(c,left_selection,right_selection)
		
		@elem.find(".widget-output").html(_.template($(@html_template_name()).html(),{conf:c,@id}))
			
		# Build the requests using the initial graph params 
		left = _.map(c.initial_graph.left_axis,(i)=>_.extend(i,@catalog[i.user]?[i.experiment]?[i.node]?[i.stream]))
		right = _.map(c.initial_graph.right_axis,(i)=>_.extend(i,@catalog[i.user]?[i.experiment]?[i.node]?[i.stream]))
		
		@prepare_and_plot(c,left,right)
	
	
	# Plot receives processed  DataRequest objects whereby the _res field contain the response received from the server as is.
	prepare_and_plot: (config,left,right)->
		data_requests = _.map({'left':left,'right':right}, (feed,direction)->
			_.map(feed, (f) -> 
				new sensordb.DataRequest({unit:f.unit,axis:direction},f.user,f.experiment,f.node,f.stream,f.fields,true,config.live,f.from_timestamp,f.to_timestamp) 
			))
		# create a request group and send it to download with a call back to the plotting function
		grouped_data_req = new sensordb.GroupedDataRequest {},_.flatten(data_requests), (req_map) => @plot(config,req_map)
		window.rm.add_grouped_data_request(grouped_data_req)
		
	plot: (conf,req_res) =>
		left_axis_unit = _.uniq(_.map(_.filter(req_res, (rr)->rr.local_info.axis == 'left'),(rr)->rr.local_info.unit))
		right_axis_unit = _.uniq(_.map(_.filter(req_res, (rr)->rr.local_info.axis == 'right'),(rr)->rr.local_info.unit))
		_.each {'left':left_axis_unit,'right':right_axis_unit},(lbl,name)->
				console.log("#{name} axis can have only one unit * #{lbl.join(",")} is not valid, first one will be used.") if (lbl.length >1)
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
			yaxes: [{position:"left",axisLabel: (if left_axis_unit.length>0 then left_axis_unit[0] else undefined),axisLabelUseCanvas: true},
			{position:"right",axisLabel: (if right_axis_unit.length>0 then right_axis_unit[0] else undefined),axisLabelUseCanvas: true}]
			grid:
				show: true
				borderWidth:1
				borderColor:"#ccc"
			
		_.extend(options,{selection:{mode: "xy"}}) if (conf.zoom)
		to_plot = _.map req_res, (rr)->
			label: "#{rr.experiment} &raquo; #{rr.node} &raquo; #{rr.stream}"
			shadowSize:conf.line_shadow
			data: rr._res.data
			yaxis: (if rr.local_info.axis is 'left' then 1 else 2)
		place_holder = @elem.find('.flot')
		if (conf.zoom)
			place_holder.unbind("plotselected").bind "plotselected", (event,ranges)=>
				plot = $.plot(place_holder,to_plot,($.extend(true, {}, options,
					xaxis:
						min: ranges.xaxis.from
						max: ranges.xaxis.to, 
					yaxis:
						min: (if ranges.yaxis is undefined then 0 else ranges.yaxis.from)
						max: (if ranges.yaxis is undefined then 0 else ranges.yaxis.to)
					y2axis:
						min: (if ranges.y2axis is undefined then 0 else ranges.y2axis.from)
						max: (if ranges.y2axis is undefined then 0 else ranges.y2axis.to) 
						axisLabel: (if right_axis_unit.length>0 then right_axis_unit[0] else undefined)
					)))
				@elem.find(".caption .reset-zoom").unbind("click").click => @plot(conf,req_res)
				start_date = parseInt((plot.getAxes()['xaxis']['min']).toFixed(0))
				end_date = parseInt((plot.getAxes()['xaxis']['max']).toFixed(0))
				# Verify the TimeZone and perform correct formatting of the timestamp
				@elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format())
				@elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format())
				
		plot = $.plot(place_holder, to_plot, options)
		start_date = plot.getAxes()['xaxis']['min']
		end_date = plot.getAxes()['xaxis']['max']
		
		@elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format())
		@elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format())
		@elem.find(".caption").show()

ws.registerWidget("WidgetTemperatureHumidityRange",WidgetTemperatureHumidityRange)