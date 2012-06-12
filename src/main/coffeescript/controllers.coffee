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
	user = $routeParams['username']
	$scope.user = user

	((lscache.get(SDB.SDB_SESSION_NAME)||{})[user]) || $resource('/session',(if user then {'user':user} else {})).get (session)->
		$rootScope.$broadcast(SDB.SESSION_INFO,session)
		$scope.experiments = session.experiments
		$scope.nodes = session.nodes
		$scope.streams = session.streams

		$scope.experiment_names = _.uniq(_.map(session.experiments,(e)->e.name))
		$scope.node_names = _.uniq(_.map(session.nodes,(e)->e.name))
		$scope.stream_names = _.uniq(_.map(session.streams,(e)->e.name))




	$(".filter-selector").on 'click' , (e)->
		src = $(e.srcElement)
		newSelection = src.text()
		parent = src.parents(".btn-group").find(".btn-label")
		oldSelection = parent.text()
		if (oldSelection isnt newSelection)
			parent.text(newSelection)

window.ExperimentCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.NodeCreateCtrl = ($scope, $location,$routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

window.StreamCreateCtrl = ($scope,$location, $routeParams,$timeout) ->
	$("body textarea").cleditor(sensordb.Utils.editor_config)

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



