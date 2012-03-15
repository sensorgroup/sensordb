root = window ? global
root.templates={}
templates =
  'layout'              : "#tmpl-layout"
  'register'            : "#tmpl-register"
  'invitation'          : "#tmpl-invitation"
  'forget_password'     : "#tmpl-forget-password"
  'welcome'             : "#tmpl-welcome"
  'login'               : "#tmpl-login"
  'mypage'              : "#tmpl-mypage"

_.each templates,(value, key) -> root.templates[key]=_.template($(value).html())

class SDB
  USERID: "sensordb_userid"

class DefaultModel extends Backbone.Model
  constructor: ->
    super

  initialize: (attributes, options) ->
    super

    @.bind "error" , (model,errors)->
      $("#error .container").html("") # reset the previous error messages, if any
      _.each errors,(msg)-> $("#error .container").append("<li>#{msg}</li>")
      $("#error").show()
  validate: (attr)->
    to_return = []
    for field in (@required_fields ? [])
      if _.isEmpty(attrs[field]) || attrs[field].trim().length is 0
        to_return.push "#{_(field).capitalize()} can't be empty"
    to_return if to_return.length !=0

class ExperimentRouter extends Backbone.Router
  routes:
    "/user/:username":  "showUserPage"

  showUserPage: (username) ->
    username ||= $.cookie SDB.USERID
    new MyPageView(el:$("body"), model:new MyPage({id:username}))
    @navigate "/user/#{username}"

class Users extends Backbone.Collection
    model: User
    url: "/user"

class User extends DefaultModel
    url: "/user/#{@id}"

    initialize: ->

    validate: (attrs) ->
        super

class Experiments extends Backbone.Collection
    model: Experiment

class Experiment extends DefaultModel
    url: -> "/experiment/#{@id}"
    required_fields:["name","timezone","description"]
    optional_fields:["url","pic"]
    validate: (attrs) ->
        super
    initialize: ->

class Nodes extends Backbone.Collection
    ur: -> "/"
    model: Node

class Node extends DefaultModel
    required_fields: ["name","location","outdoor","fixed"]
    optional_fields: ["lat","lng","alt","pic","description"]
    url: -> "/node/#{@id}"
    initialize: ->

    validate: (attrs) ->
        super

class Sensors extends Backbone.Collection
    model: Sensor

class Sensor extends DefaultModel
   url: -> "/sensor/#{@id}"
   initialize: ->
   required_fields: ["name","unit","url"]
   optional_fields: ["picture"]
   validate: (attrs) ->
        super

class UserRouter extends Backbone.Router
  routes:
    "login":  "login"
    "logout": "logout"
    "register": "register"
    "forget_password": "forget_password"
    '*path':  'defaultRoute'

  register: ->
    new UserView(el: $("body") , model:new User())
    @navigate "register"

  login: ->
    new LoginView(el: $("body") , model:new Login())
    @navigate "login"

  forget_password: ->
    new ForgottenPasswordView(el: $("body") , model:new ForgottenPassword())

  defaultRoute: -> @login()

  logout: ->
    logout_url = '/logout'
    alert("Logout called")
    @defaultRoute()

class Login extends DefaultModel
  required_fields: ["username","password"]
  url: '/login'


  login: ->
    $("form.login_form").ajaxSubmit({
      dataType: 'json'
      success: (resp, status)=>
        if(_.isEmpty(resp.userid))
          @trigger('error',@,['Login Failed, please try again'])
        else
          $.cookie SDB.USERID, resp.userid
          root.experiments.showUserPage(root.sensordb_user)
    })

class Registeration extends DefaultModel
  url: '/register'
  validate: (attrs) ->

  events:
    "click #register_btn": "register"

  register: (e) =>
    false

  render: => @el.html(root.templates['layout'](body:root.templates['register']()))

class ForgottenPassword extends DefaultModel
  url: '/forgotten_password'
  validate: (attrs) ->

class MyPage extends DefaultModel
  initialize: ->

class MyPageView extends Backbone.View
  initialize: ->
    @render()

  render: =>
    @el.html(root.templates['layout'](body:root.templates['mypage']()))

class LoginView extends Backbone.View
  initialize: -> @render()

  events:
    "click #login_btn": "login"

  login: (e) =>
    if @model.set({
      username: $("input[name='username']").val()
      password: $("input[name='password']").val()
    }) then @model.login()
    false # stop the propagation and prevent default

  render: => @el.html(root.templates['layout'](body:root.templates['login']()))

class UserView extends Backbone.View
  initialize: -> @render()
  validate: (attrs) ->
  render: => @el.html(root.templates['layout'](body:root.templates['register']()))

class ForgottenPasswordView extends Backbone.View
  initialize: -> @render()
  validate: (attrs) ->
  render: => @el.html(root.templates['layout'](body:root.templates['forget_password']()))

$ ->
  root.users = new UserRouter()
  root.experiments = new ExperimentRouter()
  Backbone.history.start()
  root.experiments.showUserPage('ali')

$ ->
  # $( "button" ).livequery -> $(@).button() # Requires JQuery UI
  $(".ds-selector li").live
    "mouseover": (e) -> $(this).addClass("highlight")
    "mouseout": (e)  -> $(this).removeClass("highlight")