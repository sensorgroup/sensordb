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
		@editor_config=
			width:'700px'
			height:250
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
	$scope.plot_data_stream_chart = ->
		options =
			series:
				lines:
					lineWidth: 1
			xaxis:
				mode: 'time'
				localTimezone: false
#			yaxes: [{position:"left",axisLabel: "Unit1",axisLabelUseCanvas: true},{position:"right"}]
			grid:
				show: true
				borderWidth:1
				borderColor:"#ccc"
			selection:
				mode: "xy"
		to_plot= [{shadowSize:2,data: [[-373597200000, 315.71], [-370918800000, 317.45], [-368326800000, 317.50]],yaxis: 1}]
		elem = $("#stream-chart")
		place_holder = elem.find('.flot')
		place_holder.unbind("plotselected").bind "plotselected", (event,ranges)=>
			plot = $.plot(place_holder,to_plot,($.extend(true, {}, options,{xaxis:{min: ranges.xaxis.from,max: ranges.xaxis.to},yaxis:{min: (if ranges.yaxis is undefined then 0 else ranges.yaxis.from),max: (if ranges.yaxis is undefined then 0 else ranges.yaxis.to)}})))
			elem.find(".caption .reset-zoom").unbind("click").click => $scope.plot_data_stream_chart()
			start_date = parseInt((plot.getAxes()['xaxis']['min']).toFixed(0))
			end_date = parseInt((plot.getAxes()['xaxis']['max']).toFixed(0))
			elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format()) # Todo: Timezones adjusted
			elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format()) # Todo: Timezones adjusted

		plot = $.plot(place_holder, to_plot, options)
		start_date = plot.getAxes()['xaxis']['min']
		end_date = plot.getAxes()['xaxis']['max']
		elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format())
		elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format())
		elem.find(".caption").show()
	user = $routeParams['username']


	$scope.hide_metadata = false
	$scope.user = user
	$resource('/measurements').query (measurements)->
		$scope.measurements =  _.reduce(measurements,((sum,item)->
			sum[item._id]=item
			sum
		),{})

	$resource('/session',(if user then {'user':user} else {})).get (session)->
		$rootScope.$broadcast(SDB.SESSION_INFO,session)
		$scope.session = session
		$scope.experiments = _.reduce(session.experiments,((sum,item)->
			sum[item._id]=item
			sum
		),{})
		$scope.nodes = _.reduce(session.nodes,(
			(sum,item)->
				sum[item._id]=item
				sum
		),{})
		$scope.streams = _.reduce(session.streams,((sum,item)->
			sum[item._id]=item
			sum
		),{})

		$scope.experiment_names = _.sortBy(_.map(session.experiments,(e)->e.name),(x)->x)
		$scope.node_names = _.sortBy(_.uniq(_.map(session.nodes,(n)->n.name)),(x)->x)
		$scope.stream_names = _.sortBy(_.uniq(_.map(session.streams,(s)->s.name)),(x)->x)

		selection_id = $routeParams.selection_id
		if(selection_id)
			$scope.selection_stream = $scope.streams[selection_id]
			$scope.selection_node = $scope.nodes[selection_id]
			$scope.selection_experiment = $scope.experiments[selection_id]
			node_ids = {}
			$scope.selection_experiment_nodes = _.reduce(session.nodes,((sum,v)->
				if v.eid is selection_id
					sum+=1
					node_ids[v._id]=1
				sum
			),0)
			$scope.selection_experiment_streams = _.reduce(session.streams,((sum,v)->
				if node_ids[v.nid]
					sum+=1
				sum
			),0)


		if ($scope.selection_stream)
			$scope.selection_sid = selection_id
			$scope.selection_nid = $scope.selection_stream.nid
			$scope.selection_eid = $scope.nodes[$scope.selection_nid].eid
		if ($scope.selection_node)
			$scope.selection_sid = undefined
			$scope.selection_nid = selection_id
			$scope.selection_eid = $scope.selection_node.eid
			$scope.selection_node_streams = _.reduce(session.streams,((sum,v)->
				if v.nid is selection_id
					sum+=1
				sum
			),0)
		if ($scope.selection_experiment)
			$scope.selection_sid = undefined
			$scope.selection_nid = undefined
			$scope.selection_eid = selection_id

		apply_selection_filters($scope.session)
	$("body").on "click",(e)->
		src = $(e.srcElement)
		if src.attr("id") is "show_hide_metadata"
			$scope.hide_metadata = !$scope.hide_metadata

	$(".filter-selector").on 'click' , (e)->
		src = $(e.srcElement)
		newSelection = src.text()
		parent = src.parents(".btn-group").find(".btn-label")
		oldSelection = parent.text()
		if (oldSelection isnt newSelection)
			parent.text(newSelection)
			selected_stream = _.filter([$("#stream-filter .btn-label").text()],(i)->i isnt "All Streams")[0]
			selected_node = _.filter([$("#node-filter .btn-label").text()],(i)->i isnt "All Nodes")[0]
			selected_exp = _.filter([$("#experiment-filter .btn-label").text()],(i)-> i isnt "Choose an Experiment")[0]
			apply_selection_filters(selected_stream,selected_node,selected_exp,$scope.session)

	apply_selection_filters = (session,selected_stream,selected_node,selected_exp)->
		if (selected_exp || selected_node || selected_stream)
			streams = if selected_stream then (_.filter session.streams,(s)-> s.name is selected_stream) else session.streams
			stream_nids = _.map streams,(s)->s.nid
			nodes = if selected_node then (_.filter session.nodes,(n)-> n.name is selected_node) else (if selected_stream then _.filter(session.nodes,(n)-> _.indexOf(stream_nids,n._id)>=0) else session.nodes)
			node_eids = _.map nodes,(n)->n.eid
			experiments = if selected_exp then (_.filter session.experiments,(e)-> e.name is selected_exp) else  _.filter session.experiments,(e)-> _.indexOf(node_eids,e._id)>=0
			experiment_ids = _.map experiments,(i)->i._id
			nodes = _.filter nodes, (n)-> _.indexOf(experiment_ids,n.eid)>=0
			node_ids = _.map nodes, (n)->n._id
			streams = _.filter streams, (s)-> _.indexOf(node_ids,s.nid)>=0
			experiment_names = _.sortBy(_.map(experiments,(e)->e.name),(x)->x)
			node_names = _.sortBy(_.uniq(_.map(nodes,(n)->n.name)),(x)->x)
			stream_names = _.sortBy(_.uniq(_.map(streams,(s)->s.name)),(x)->x)
		else
			seen_nodes = {}
			seen_exps = {}
			filtered_rows = []
			stream_node_exp = _.reduce($scope.streams,((sum,v)->
				exp_id = $scope.nodes[v.nid].eid
				sum.push({s:v._id,n:v.nid,e:exp_id})
				seen_nodes[v.nid] = 1
				seen_exps[exp_id] = 1
				sum
			),filtered_rows)

			node_exp = _.reduce(_.difference( _.keys($scope.nodes),_.keys(seen_nodes))||[],((sum,v)->
				node = $scope.nodes[v]
				seen_exps[node.eid]=1
				seen_nodes[node._id]=1
				sum.push({n:node._id,e:node.eid})
				sum
			),filtered_rows)
			exp = _.reduce(_.difference(_.keys($scope.experiments),_.keys(seen_exps)),((sum,eid)->sum.push({e:eid});sum),filtered_rows)

			$scope.filtered_rows = _.union(stream_node_exp,node_exp,exp)
	$scope.period = -1
	$scope.set_period = (period)->
		if (period == $scope.period )
			return
		$scope.period = period
		all_streams = _($scope.filtered_rows).map((v)->v.s).filter((v)->v isnt undefined)
#		$resource('/data',{"sid":all_streams,}).get (session)->


window.ExperimentCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.NodeCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.StreamCreateCtrl = ($scope,$location, $routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.DataExplorerCtrl = ($scope) ->

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



