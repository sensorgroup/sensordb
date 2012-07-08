'use strict'
$ ->
	$.ajaxSetup
		beforeSend: -> $('.ajax-loading img').show()
		complete: -> $('.ajax-loading img').hide()

class SDB
	@INVALIDATE_SESSION='invalidate-session'
	@LOGOUT='logout'
	@SESSION_INFO='session-info'
	@SDB_SESSION_NAME="sdb" # This is set by server, can't change at client side.
	@ERROR_ALERT_MESSAGE="error-alert-msg"
	@SUCCESS_ALERT_MESSAGE="success-alert-msg"
	@CLEAR_ALERT_MESSAGE="CLEAR_ALERT_MESSAGE"
	@USER_NAME="username"

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

			to_plot = _.map req_res, (rr)->
				shadowSize:1
				data: rr._res.data
				yaxis: 1
			place_holder = @elem.find('.flot')
			place_holder.unbind("plotselected").bind "plotselected", (event,ranges)=>
				plot = $.plot(place_holder,to_plot,($.extend(true, {}, options,
					xaxis:
						min: ranges.xaxis.from
						max: ranges.xaxis.to
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

	class @Utils
		@calc_std = (sum,sumSq,count)-> Math.sqrt((sumSq - sum*sum/count) / (count-1)) #square root of[(sum of Xsquared -((sum of X)*(sum of X)/N))/(N-1)]
		@editor_config=
			width:'700px'
			height:"250px"
			controls:"bold italic underline strikethrough subscript superscript | color highlight | bullets numbering | outdent " +
			"indent | alignleft center alignright justify | undo redo | " +
			"image link unlink | cut copy paste | source",
			colors:"FFF FCC FC9 FF9 FFC 9F9 9FF CFF CCF FCF " +
			"CCC F66 F96 FF6 FF3 6F9 3FF 6FF 99F F9F " +
			"BBB F00 F90 FC6 FF0 3F3 6CC 3CF 66C C6C " +
			"999 C00 F60 FC3 FC0 3C0 0CC 36F 63F C3C " +
			"666 900 C60 C93 990 090 399 33F 60C 939 " +
			"333 600 930 963 660 060 366 009 339 636 " +
			"000 300 630 633 330 030 033 006 309 303",
			fonts:"Arial,Arial Black,Comic Sans MS,Courier New,Narrow,Garamond," +
			"Georgia,Impact,Sans Serif,Serif,Tahoma,Trebuchet MS,Verdana"
			useCSS:false
			docType:'<!DOCTYPE html>'
			docCSSFile:""
			bodyStyle:"margin:4px; font:10pt Arial,Verdana; cursor:text"

apply_tooltips = ()->
	$("a [rel=tooltip]").tooltip()
	$("a [rel=tooltip]").click(->$(this).tooltip('hide')) # this is required for single page apps as page rewrite even is not received by tooltip


window.LoginCtrl = ($scope, $location,$routeParams) ->

window.LogoutCtrl = ($scope,$location, $routeParams) ->

window.RegistrationCtrl = ($scope,$location, $routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.HomeCtrl = ($scope, $location,$routeParams,$cookies,$resource,$timeout) ->
	$scope.users=$resource("/users").query ()->	$timeout(apply_tooltips,500)

window.AnalysisCtrl = ($scope, $location,$routeParams) ->

window.DataPageCtrl = ($scope,$rootScope, $location,$routeParams,$resource) ->
	$scope.local_tz_offset = (new Date()).getTimezoneOffset()*60
	$scope.calc_std = sensordb.Utils.calc_std
	selection_id = $routeParams.selection_id

	$scope.plot_data_stream_chart = ->
		options =
			series:
				lines:
					lineWidth: 1
			xaxis:
				mode: 'time'
				localTimezone: false
			grid:
				show: true
				borderWidth:1
				borderColor:"#ccc"
			selection:
				mode: "xy"
		to_plot= [{shadowSize:0,data: (if $scope.agg_period is "raw" then _.map($scope.data,(d)->[(d[0] - 2*$scope.local_tz_offset)*1000,d[1]]) else _.map($scope.data,(d)->[((d[0]+d[1]) / 2)*1000,d[7] / d[6]])),yaxis: 1}]
		place_holder = $('#flot')
		place_holder.unbind("plotselected").bind "plotselected", (event,ranges)=>
			plot = $.plot(place_holder,to_plot,($.extend(true, {}, options,{xaxis:{min: ranges.xaxis.from,max: ranges.xaxis.to},yaxis:{min: (if ranges.yaxis is undefined then 0 else ranges.yaxis.from),max: (if ranges.yaxis is undefined then 0 else ranges.yaxis.to)}})))
			$scope.plot_from = ranges.xaxis.from
			$scope.plot_to = ranges.xaxis.to
			$scope.$apply()

		plot = $.plot(place_holder, to_plot, options)

	user = $routeParams['username']
	$scope.hide_metadata = false
	$scope.user = user
	$resource('/measurements').query (measurements)->
		$scope.measurements =  _.reduce(measurements,((sum,item)->
			sum[item._id]=item
			sum
		),{})

	$scope.set_agg_period = (period) ->
		$resource("/data",{sid:selection_id,level:period}).get (data)->
			$scope.agg_period = period
			$scope.table_time_format = switch period
				when "raw" then 'dd MMM yyyy - HH:mm:ss'
				when "1-hour" then 'dd MMM yyyy - HHa'
				when "1-day" then 'dd MMM yyyy'
				when "1-month" then 'MMMM yyyy'
				when "1-year" then 'yyyy'
			$scope.data= _.sortBy(data[selection_id],(d)->d[0]) #d[0] is minTs, default sort is by timestamp
			$scope.plot_from = $scope.first_updated = ($scope.data[0][0] - $scope.local_tz_offset)*1000
			$scope.plot_to = $scope.last_updated = ($scope.data[$scope.data.length-1][if period is "raw" then 0 else 1] - $scope.local_tz_offset)*1000
			$scope.plot_data_stream_chart()

	$resource('/session',(if user then {'user':user} else {})).get (session)->
		$rootScope.$broadcast(SDB.SESSION_INFO,session)
		$scope.session = session
		if(selection_id)
			$scope.selection_stream = _.find($scope.session.streams, (s)->s._id is selection_id)
			$scope.selection_node = _.find($scope.session.nodes, (n)->n._id is selection_id)
			$scope.selection_experiment = _.find($scope.session.experiments, (e)->e._id is selection_id)
		# Things to set if a STREAM is selected, used by data_page_stream.html
		if ($scope.selection_stream)
			$scope.selection_node = _.find($scope.session.nodes, (n)->n._id is $scope.selection_stream.nid)
			$scope.selection_experiment = _.find($scope.session.experiments, (e)->e._id is $scope.selection_node.eid)
			$scope.set_agg_period("1-day")
		# Things to set if a NODE is selected, used by data_page_node.html
		if ($scope.selection_node)
			$scope.selection_experiment =  _.find($scope.session.experiments, (e)->e._id is $scope.selection_node.eid)
			$scope.selection_node_streams = _.reduce(session.streams,((sum,v)->
				if v.nid is selection_id
					sum+=1
				sum
			),0)
		# Things to set if an EXPERIMENT is selected, used by data_page_experiment.html
		if ($scope.selection_experiment)
			# This is used to show number of nodes per experiment, if the selection is an experiment - used by data_page_experiment
			node_ids = {}
			$scope.selection_experiment_nodes = _.reduce(session.nodes,((sum,v)->
				if v.eid is selection_id
					sum+=1
					node_ids[v._id]=1
				sum
			),0)
			# This is used to show number of streams per experiment, used by data_page_experiment
			$scope.selection_experiment_streams = _.reduce(session.streams,((sum,v)->
				if node_ids[v.nid]
					sum+=1
				sum
			),0)

	$("body").on "click",(e)->
		src = $(e.srcElement)
		if src.attr("id") is "show_hide_metadata"
			$scope.hide_metadata = !$scope.hide_metadata

	$scope.period = -1
	$scope.set_period = (period)->
		if (period == $scope.period )
			return
		$scope.period = period

window.ExperimentCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.NodeCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.StreamCreateCtrl = ($scope,$location, $routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.DataExplorerCtrl = ($scope,$routeParams,$resource,$rootScope) ->
	$scope.user = user = $routeParams['username']
	$scope.calc_std = sensordb.Utils.calc_std
	$scope.local_tz_offset = (new Date()).getTimezoneOffset()*60
	$resource('/measurements').query (measurements)->
		$scope.measurements =  _.reduce(measurements,((sum,item)->
			sum[item._id]=item
			sum
		),{})

	$resource('/session',(if user then {'user':user} else {})).get (session)->
		$rootScope.$broadcast(SDB.SESSION_INFO,session)
		$scope.session = session
		$scope.experiment_names = _.sortBy(_.map(session.experiments,(e)->e.name),(x)->x)
		$scope.node_names = _.sortBy(_.uniq(_.map(session.nodes,(n)->n.name)),(x)->x)
		$scope.stream_names = _.sortBy(_.uniq(_.map(session.streams,(s)->s.name)),(x)->x)

		$scope.experiments = _.reduce(session.experiments,((sum,item)->
			sum[item._id]=item
			sum
		),{})
		$scope.nodes = _.reduce(session.nodes,((sum,item)->
			sum[item._id]=item
			sum
		),{})
		$scope.streams = _.reduce(session.streams,((sum,item)->
			sum[item._id]=item
			sum
		),{})
		$scope.apply_filters()

	$scope.apply_filters = ()->
		source_filter = apply_source_filter()
		filtering_results = source_filter
		# Shoud I use Lscache ? not sure as I need to then make sure in-browser cache invalidated when a new entry available !
		if (source_filter.s?.length > 0)
			$resource("/data",{sid:JSON.stringify(source_filter.s),level:"1-year"}).get (summaries)->
				summaries = _.reduce(summaries,(summary,values,key)->
					summary[key] =
						if(values.length>1)
							minTs = values[0][0]
							maxTs = values[values.length-1][1]
							minTsVal = values[0][2]
							maxTsVal = values[values.length-1][3]
							min =  _.reduce(values,((sum,num)-> Math.min(sum,num[4])),values[0][4])
							max =  _.reduce(values,((sum,num)-> Math.max(sum,num[5])),values[0][5])
							count = _.reduce(values,((sum,num)->sum+num[6]),0)
							total = _.reduce(values,((sum,num)->sum+num[7]),0)
							sumSq = _.reduce(values,((sum,num)->sum+num[8]),0)
							[minTs,maxTs,minTsVal,maxTsVal,min,max,count,total,sumSq]
						else
							values[0]
					 summary
				,{})
				console.log(summaries)
				filtering_results.data = summaries
				$scope.filtering_results=filtering_results
		else
			$scope.filtering_results=filtering_results

	apply_source_filter = ()->
		# Select experiments based on their names, return experiment.id as array
		experiments_tmp = _.map((if $scope.experiment_selector != undefined && $scope.experiment_selector !=""
			_.filter $scope.session.experiments,(item) -> item.name is $scope.experiment_selector
		else
			$scope.session.experiments)
			,(e)->e._id)
		# Select nodes based on their names
		nodes_tmp = if $scope.node_selector != undefined && $scope.node_selector !=""
			_.filter $scope.session.nodes,(item) -> item.name is $scope.node_selector
		else
			$scope.session.nodes
		# Select streams based on their names
		streams_tmp = if ($scope.stream_selector != undefined && $scope.stream_selector !="" )
			_.filter $scope.session.streams,(item) -> item.name is $scope.stream_selector
		else
			$scope.session.streams
		# Select streams based on the measurement type
		streams_tmp = if ($scope.measurement_selector != undefined && $scope.measurement_selector !="" )
			_.filter streams_tmp,(item) -> item.mid is $scope.measurement_selector
		else
			streams_tmp
		# Select only nodes that a belonging to the selected experiment(s)
		nodes_tmp = _.filter(nodes_tmp,(n)-> _.include(experiments_tmp,n.eid))
		# Select only nodes that a belonging to the selected streams(s)
		stream_node_ids = _.map(streams_tmp,(s)->s.nid)
		nodes_tmp = _.filter nodes_tmp,(n)-> _.include(stream_node_ids,n._id)
		# Select only streams that a belonging to the selected nodes(s)
		nodes_tmp_id = _.map nodes_tmp,(n)->n._id
		streams_tmp = _.filter(streams_tmp,(s)-> _.include(nodes_tmp_id,s.nid))
		# Select only experiments that a belonging to the selected nodes(s)
		nodes_tmp_eid = _.map nodes_tmp,(n)->n.eid
		experiments_tmp = _.filter(experiments_tmp,(e)-> _.include(nodes_tmp_eid,e))
		{e:experiments_tmp,n: _.map(nodes_tmp,(n)->n._id),s:_.map(streams_tmp,(s)->s._id)}

# Join the experiments, nodes and streams using outer join

window.Err404Ctrl = ($scope) ->
	$scope.url=window.location.href

window.HeaderCtrl = ($scope,$rootScope,$cookies,$timeout,$resource)->
	$resource('/session').get (session)->	$rootScope.$broadcast(SDB.SESSION_INFO,session)

	$scope.$on SDB.CLEAR_ALERT_MESSAGE, -> $timeout((()->$scope.success_messages=$scope.error_messages=undefined),1500)

	$scope.$on SDB.ERROR_ALERT_MESSAGE, (scopy,messages)->
		$scope.error_messages=messages
		$scope.success_messages = undefined
		$("body").scrollTop(0)

		$scope.$broadcast(SDB.CLEAR_ALERT_MESSAGE)
		$scope.$apply()

	$scope.$on SDB.SUCCESS_ALERT_MESSAGE, (scopy,messages)->
		$scope.success_messages=messages
		$scope.error_messages = undefined
		$("body").scrollTop(0)
		$scope.$broadcast(SDB.CLEAR_ALERT_MESSAGE)
		$scope.$apply()

	$scope.$on SDB.LOGOUT, ()->
		$scope.loggedIn=false
		$rootScope.$broadcast(SDB.SUCCESS_ALERT_MESSAGE,["Logged out Successfully"])
		$("#login-name").val("Username")
		$("#login-password").val("password")
		$scope.errors=undefined
		$scope.$apply()

	$scope.$on SDB.SESSION_INFO, (msg_name,session)->
		return unless session?.user?.name
		cache = (lscache.get(SDB.SDB_SESSION_NAME) || {})
		cache[session.user.name] = session
		#		lscache.set(SDB.SDB_SESSION_NAME,cache)
		if(session?.user?.email) # email because only logged in user can see his email
			$scope.username =session?.user?.name
			lscache.set(SDB.USER_NAME,session?.user?.name)
		$scope.loggedIn = lscache.get(SDB.USER_NAME)

	$scope.$on SDB.INVALIDATE_SESSION, ()->
		lscache.flush()

	$("body").on "click","a#login-btn",(e)->
		credentials = {name:$("#login-name").val(), password:$("#login-password").val()}

		$.ajax type:'post', url:'/login', data:credentials , success:((res)->
			session_info =jQuery.parseJSON(res)

			$rootScope.$broadcast(SDB.SESSION_INFO,session_info)
			$rootScope.$broadcast(SDB.SUCCESS_ALERT_MESSAGE,["Logged in Successfully"])
		) , error:(res)-> $rootScope.$broadcast(SDB.ERROR_ALERT_MESSAGE,jQuery.parseJSON(res.responseText)["errors"])


	$("body").on "click","a#logout-btn",(e)->
		$.ajax type:'post',url:'/logout' , success:()->
			$rootScope.$broadcast(SDB.INVALIDATE_SESSION)
			$rootScope.$broadcast(SDB.LOGOUT)


window.FooterCtrl = ($scope,$location)->
	$scope.isHomePage = $location.url() is '/'



