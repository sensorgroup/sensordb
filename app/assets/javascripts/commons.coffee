_.mixin(_.string.exports())

window.module = (name, fn)->
  if not @[name]?
    this[name] = {}
  if not @[name].module?
    @[name].module = window.module
  fn.apply(this[name], [])

$LAB.setGlobalDefaults({Debug:true})

Date::utc_format = -> @getUTCDate()+"/"+@getUTCMonth()+"/"+@getUTCFullYear()+" "+@getUTCHours()+":"+@getUTCMinutes()+":"+@getUTCSeconds()

alertFallback = true
if (typeof window.console is "undefined" || typeof window.console.log is "undefined") 
	window.console = {}
	if (alertFallback)
		window.console.log = (msg)->alert(msg)
	else 
		windlow.console.log = ->