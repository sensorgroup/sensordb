(function() {
  var Router, SampleDatabase, WidgetStore;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) {
    for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; }
    function ctor() { this.constructor = child; }
    ctor.prototype = parent.prototype;
    child.prototype = new ctor;
    child.__super__ = parent.prototype;
    return child;
  };
  this.module("sensordb", function() {
    var Experiment, Experiments;
    this.show_errors = function(res) {
      var errors;
      errors = _.reduce((_.defaults(jQuery.parseJSON(res.responseText), {
        "errors": []
      }))["errors"], (function(sum, msg) {
        return sum + ("<p>" + msg + "</p>");
      }), "");
      if (errors.length > 0) {
        $(".alert-error").show().find("div.messages").html(errors);
        return sensordb.Utils.scroll_top();
      }
    };
    this.show_alert = function(selector, messages) {
      messages = _.reduce(messages, (function(sum, msg) {
        return sum + ("<p>" + msg + "</p>");
      }), "");
      if (messages.length > 0) {
        $(selector).show().find("div.messages").html(messages);
        return sensordb.Utils.scroll_top();
      }
    };
    this.Utils = (function() {
      function Utils() {}
      Utils.editor_config = {
        width: '700px',
        height: 250,
        controls: "bold italic underline strikethrough subscript superscript | color highlight | bullets numbering | outdent " + "indent | alignleft center alignright justify | undo redo | " + "image link unlink | cut copy paste | source",
        colors: "FFF FCC FC9 FF9 FFC 9F9 9FF CFF CCF FCF " + "CCC F66 F96 FF6 FF3 6F9 3FF 6FF 99F F9F " + "BBB F00 F90 FC6 FF0 3F3 6CC 3CF 66C C6C " + "999 C00 F60 FC3 FC0 3C0 0CC 36F 63F C3C " + "666 900 C60 C93 990 090 399 33F 60C 939 " + "333 600 930 963 660 060 366 009 339 636 " + "000 300 630 633 330 030 033 006 309 303",
        fonts: "Arial,Arial Black,Comic Sans MS,Courier New,Narrow,Garamond," + "Georgia,Impact,Sans Serif,Serif,Tahoma,Trebuchet MS,Verdana",
        useCSS: false,
        docType: '<!DOCTYPE html>',
        docCSSFile: "",
        bodyStyle: "margin:4px; font:10pt Arial,Verdana; cursor:text"
      };
      Utils.scroll_top = function() {
        $("body").scrollTop(0);
        return this.guid = function() {
          var S4;
          S4 = function() {
            return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
          };
          return S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4();
        };
      };
      Utils.find_in_catalog_by_stream_id = function(stream_id, catalog) {
        var exp_name, experiments, node_name, nodes, s_id, stream_name, streams, user;
        for (user in catalog) {
          experiments = catalog[user];
          for (exp_name in experiments) {
            nodes = experiments[exp_name];
            for (node_name in nodes) {
              streams = nodes[node_name];
              for (stream_name in streams) {
                s_id = streams[stream_name];
                if (s_id.sid === stream_id) {
                  return [user, exp_name, node_name, stream_name, s_id.unit];
                }
              }
            }
          }
        }
      };
      return Utils;
    })();
    this.DataRequest = (function() {
      function DataRequest(local_info, user_id, experiment, node, stream, fields, checkCache, live, from, to) {
        this.local_info = local_info;
        this.user_id = user_id;
        this.experiment = experiment;
        this.node = node;
        this.stream = stream;
        this.fields = fields;
        this.checkCache = checkCache;
        this.live = live;
        this.from = from;
        this.to = to;
        this.id = [
          this.user_id, this.experiment, this.node, this.stream, _.map(this.fields, function(v, k) {
            return "" + k + "->" + v;
          }), this.checkCache, this.live, this.from, this.to
        ].join('$');
      }
      return DataRequest;
    })();
    this.GroupedDataRequest = (function() {
      function GroupedDataRequest(local_info, data_requests, call_back) {
        this.local_info = local_info;
        this.data_requests = data_requests;
        this.call_back = call_back;
        _.each(this.data_requests, function(r) {
          return r._res = void 0;
        });
        this.id = _.map(this.data_requests, function(req) {
          return req.id;
        }).join("$$");
        this.live = _.any(this.data_requests, function(r) {
          return r.live;
        });
      }
      GroupedDataRequest.prototype.fire = function() {
        return this.call_back(this.data_requests);
      };
      return GroupedDataRequest;
    })();
    this.GroupedRequestManager = (function() {
      function GroupedRequestManager(db) {
        this.db = db;
        this.remove_grouped_data_request = __bind(this.remove_grouped_data_request, this);
        this.listeners = {};
      }
      GroupedRequestManager.prototype.add_grouped_data_request = function(grouped_data_request) {
        if (!this.listeners[grouped_data_request.id]) {
          this.listeners[grouped_data_request.id] = grouped_data_request;
          return _.each(grouped_data_request.data_requests, __bind(function(req) {
            return this.db.download(req, __bind(function(res) {
              return this.data_received(req.id, res);
            }, this));
          }, this));
        } else {
          return console.log("This request " + grouped_data_request.id + " exists in the listener's list.");
        }
      };
      GroupedRequestManager.prototype.remove_grouped_data_request = function(grouped_data_request) {
        return delete this.listeners[grouped_data_request.id];
      };
      GroupedRequestManager.prototype.data_received = function(data_request_id, data_response) {
        return _.each(this.listeners, __bind(function(grouped_data_request) {
          var anything_to_set;
          anything_to_set = _.filter(grouped_data_request.data_requests, __bind(function(req) {
            return req.id === data_request_id && (!req._res || (req._res && req.live));
          }, this));
          if (anything_to_set.length > 0) {
            _.each(anything_to_set, function(r) {
              return r._res = data_response;
            });
            if (_.all(grouped_data_request.data_requests, function(req) {
              return req._res !== void 0;
            })) {
              grouped_data_request.fire();
              if (!grouped_data_request.live) {
                delete this.listeners[grouped_data_request.id];
                return console.debug("Grouped Data Request " + grouped_data_request.id + " is triggered and removed (not live).");
              }
            }
          }
        }, this));
      };
      return GroupedRequestManager;
    })();
    this.Database = (function() {
      function Database() {}
      Database.prototype.analysis = function(username) {};
      Database.prototype.download = function(data_request, callback) {};
      Database.prototype.catalog = function(selectors) {};
      return Database;
    })();
    this.Widget = (function() {
      __extends(Widget, Backbone.Events);
      function Widget(widget) {
        this.widget = widget;
        this.db = window.db;
        this.elem = $("\#" + this.widget.html_id);
        this.id = this.widget.html_id;
        this.process_config(this.widget.conf);
      }
      Widget.prototype.dataFeeds = function(conf) {
        return alert('not implemented');
      };
      Widget.prototype.process_config = function(new_conf) {};
      Widget.prototype.sample_config = function() {
        return alert('not implemented');
      };
      return Widget;
    })();
    Experiment = (function() {
      __extends(Experiment, Backbone.Model);
      function Experiment() {
        Experiment.__super__.constructor.apply(this, arguments);
      }
      Experiment.prototype.defaults = function() {
        return {
          user_id: 123
        };
      };
      Experiment.prototype.initialize = function() {};
      Experiment.prototype.validate = function(attrs) {};
      return Experiment;
    })();
    return Experiments = (function() {
      __extends(Experiments, Backbone.Collection);
      function Experiments() {
        Experiments.__super__.constructor.apply(this, arguments);
      }
      Experiments.prototype.url = '/experiments';
      Experiments.prototype.model = Experiment;
      return Experiments;
    })();
  });
  WidgetStore = (function() {
    function WidgetStore() {
      this.widgets = {};
      this.active_widgets = [];
    }
    WidgetStore.prototype.registerWidget = function(widget_name, widget) {
      return this.widgets[widget_name] = widget;
    };
    WidgetStore.prototype.getWidgetFor = function(widget_name) {
      return this.widgets[widget_name];
    };
    WidgetStore.prototype.handler2ClassName = function(widget_name) {
      return _('-' + widget_name).camelize();
    };
    WidgetStore.prototype.className2Handler = function(className) {
      return _(className).underscored();
    };
    WidgetStore.prototype.addWidgetInstnace = function(widget_instance) {
      return this.active_widgets.push(widget_instance);
    };
    return WidgetStore;
  })();
  window.ws = new WidgetStore();
  SampleDatabase = (function() {
    __extends(SampleDatabase, sensordb.Database);
    function SampleDatabase() {
      SampleDatabase.__super__.constructor.apply(this, arguments);
    }
    SampleDatabase.prototype.analysis = function(username) {
      return {
        'analysis1': {
          'Temperature Range and Humidity Widget': {
            handler: 'WidgetTemperatureHumidityRange',
            description: "Use this widget to plot raw sensor data. You can configure individual axies using the following menu.",
            conf: {
              width: '100%',
              height: '250px',
              zoom: false,
              line_shadow: 0,
              allow_axis_selection: true,
              from_date: '10/10/2004',
              to_date: '',
              feeds: ['ali/Yanco', 'ali/Yanco2'],
              live: false,
              initial_graph: {
                left_axis: [
                  {
                    user: 'ali',
                    experiment: 'Yanco1',
                    node: 'Janz [Irrigated]',
                    stream: 'Canopy Temperature',
                    from_timestamp: 1331439787953,
                    to_timestamp: void 0,
                    fields: {
                      data: 1
                    }
                  }, {
                    user: 'ali',
                    experiment: 'Yanco2',
                    node: 'Janz [Irrigated]',
                    stream: 'Canopy Temperature',
                    from_timestamp: 1331439787953,
                    to_timestamp: void 0,
                    fields: {
                      data: 1
                    }
                  }
                ],
                right_axis: [
                  {
                    user: 'ali',
                    experiment: 'Yanco1',
                    node: 'Janz [Irrigated]',
                    stream: 'Canopy Temperature',
                    from_timestamp: 1331439787953,
                    to_timestamp: void 0,
                    fields: {
                      data: 1
                    }
                  }
                ]
              }
            }
          },
          'Degree Days Widget': {
            handler: 'WidgetEt0Vpd',
            conf: {
              from_date: '10/10/2004',
              to_date: ''
            }
          }
        },
        'analysis2': {}
      };
    };
    SampleDatabase.prototype.catalog = function(selectors) {
      return {
        'ali': {
          'Yanco1': {
            'Janz [Irrigated]': {
              'Canopy Temperature': {
                sid: '1',
                unit: 'Temperature (C)'
              },
              'Body Temperature': {
                sid: '2',
                unit: 'Light'
              }
            },
            'Weather': {
              'PAR': {
                sid: '3',
                unit: 'Humidity (H)'
              },
              'Wind Speed (Max)': {
                sid: '4',
                unit: 'Temperature (C)'
              },
              'Wind Speed (Average)': {
                sid: '5',
                unit: 'Temperature (C)'
              },
              'Air Pressure': {
                sid: '6',
                unit: 'Temperature (C)'
              },
              'Air Pressure (Sea Level)': {
                sid: '7',
                unit: 'Temperature (C)'
              },
              'Relative Humidity': {
                sid: '8',
                unit: 'Temperature (C)'
              },
              'Wind Speed (Min)': {
                sid: '9',
                unit: 'Temperature (C)'
              },
              'Solar Flux': {
                sid: '10',
                unit: 'Temperature (C)'
              },
              'Air Temperature': {
                sid: '11',
                unit: 'Temperature (C)'
              },
              'Rain Accumulation': {
                sid: '12',
                unit: 'Temperature (C)'
              }
            }
          },
          'Yanco2': {
            'Janz [Irrigated]': {
              'Canopy Temperature': {
                sid: '13',
                unit: 'Temperature (C)'
              },
              'Body Temperature': {
                sid: '14',
                unit: 'Temperature (C)'
              },
              'Weather': {
                sid: '15',
                unit: 'Temperature (C)'
              },
              'Wind Speed (Max)': {
                sid: '16',
                unit: 'Temperature (C)'
              },
              'Wind Speed (Average)': {
                sid: '17',
                unit: 'Temperature (C)'
              },
              'Air Pressure': {
                sid: '18',
                unit: 'Temperature (C)'
              },
              'Air Pressure (Sea Level)': {
                sid: '19',
                unit: 'Temperature (C)'
              },
              'Relative Humidity': {
                sid: '20',
                unit: 'Temperature (C)'
              },
              'Wind Speed (Min)': {
                sid: '21',
                unit: 'Temperature (C)'
              },
              'Solar Flux': {
                sid: '22',
                unit: 'Temperature (C)'
              },
              'Air Temperature': {
                sid: '23',
                unit: 'Temperature (C)'
              },
              'Rain Accumulation': {
                sid: '24',
                unit: 'Temperature (C)'
              }
            }
          }
        }
      };
    };
    SampleDatabase.prototype.download = function(data_request, callback) {
      var sample, start_time;
      start_time = (new Date()).getTime();
      sample = {
        label: "Sample Data " + (Math.random() * 100),
        data: [],
        id: data_request.id
      };
      _.times(Math.random() * 100 + 100, function(i) {
        return sample.data.push([start_time + i * 100, Math.random() * 10 + i]);
      });
      return callback(sample);
    };
    return SampleDatabase;
  })();
  window.db = new SampleDatabase();
  window.rm = new sensordb.GroupedRequestManager(window.db);
  Router = (function() {
    var first_page_tpl;
    __extends(Router, Backbone.Router);
    function Router() {
      Router.__super__.constructor.apply(this, arguments);
    }
    Router.prototype.routes = {
      "": "home",
      ":user/analysis/:name": "analysis",
      "experiments/create": "create_experiment",
      "nodes/create": "create_node",
      "streams/create": "create_stream",
      ":user/data": "data_page",
      "register": "register",
      "test": "test",
      '*path': 'error404'
    };
    Router.prototype.test = function() {
      return this.session();
    };
    Router.prototype.session = function(callback_func) {
      var session_name;
      if (_.isUndefined(callback_func)) {
        return;
      }
      session_name = "sdb-session";
      if (_.isNull(lscache.get(session_name)) || _.isEmpty(lscache.get(session_name))) {
        return $.ajax({
          type: 'get',
          url: '/session',
          success: (function(res) {
            var session_info;
            session_info = jQuery.parseJSON(res);
            lscache.set(session_name, session_info);
            return callback_func(session_info);
          }),
          error: (function(errors) {
            sensordb.show_errors(errors);
            return callback_func({});
          })
        });
      } else {
        return callback_func(lscache.get(session_name));
      }
    };
    Router.prototype.default_route = function() {
      return this.navigate("/#");
    };
    first_page_tpl = "#tpl-first-page";
    Router.prototype.layout = function(template_id, template_params, callback_func) {
      if (template_params == null) {
        template_params = {};
      }
      if (callback_func == null) {
        callback_func = function(session) {
          return {};
        };
      }
      return this.session(function(session) {
        session || (session = {});
        $("body div#navigation").html(_.template($("#navbar-tpl").html(), {
          session: session
        }));
        if (template_id !== first_page_tpl) {
          $("body div#header").html(_.template($("#header-tpl").html()));
        } else {
          $("body div#header").html("");
        }
        $("body div#contents").html(_.template($(template_id).html(), template_params));
        if (template_id !== first_page_tpl) {
          $("body div#footer").html(_.template($("#footer-tpl").html()));
        } else {
          $("body div#footer").html("");
        }
        sensordb.Utils.scroll_top();
        return callback_func(session);
      });
    };
    Router.prototype.register = function() {
      return this.layout("#tpl-register", {}, function() {
        $("body textarea").cleditor(sensordb.Utils.editor_config);
        return $("#registration a.btn-primary").click(function() {
          return $.ajax({
            type: 'post',
            url: '/register',
            data: $("#registration").serialize(),
            success: (function(res) {
              return console.log(res);
            }),
            error: sensordb.show_errors
          });
        });
      });
    };
    Router.prototype.data_page = function(username) {
      return this.layout("#tpl-data-page", {}, function() {
        return $("#data-table").tablesorter();
      });
    };
    Router.prototype.analysis = function(user, name) {
      var widgets;
      widgets = db.analysis(user)[name];
      _.each(widgets, function(value) {
        value.html_id = sensordb.Utils.guid();
        return $LAB.script("/j/" + value.handler + ".js").wait(function() {
          if (window.ws.getWidgetFor(value.handler)) {
            return window.ws.addWidgetInstnace(new (window.ws.getWidgetFor(value.handler))(value));
          } else {
            return alert("Failed! Loading widget " + value.handler);
          }
        });
      });
      return this.layout("#tpl-analysis", {
        widgets: widgets
      });
    };
    Router.prototype.home = function() {
      return this.layout(first_page_tpl, {}, function(session) {
        $("[rel=tooltip]").tooltip();
        return $("a [rel=tooltip]").click(function() {
          return $(this).tooltip('hide');
        });
      });
    };
    Router.prototype.create_experiment = function() {
      return this.layout("#tpl-experiment-create", {}, function() {
        $("body textarea").cleditor(sensordb.Utils.editor_config);
        return $(".container form").ajaxForm(function() {
          return alert("Thank you for your comment!");
        });
      });
    };
    Router.prototype.create_node = function() {
      return this.layout("#tpl-node-create", {}, function() {
        $("body textarea").cleditor(sensordb.Utils.editor_config);
        return $(".container form").ajaxForm(function() {
          return alert("Thank you for your comment!");
        });
      });
    };
    Router.prototype.create_stream = function() {
      return this.layout("#tpl-stream-create", {}, function() {
        $("body textarea").cleditor(sensordb.Utils.editor_config);
        return $(".container form").ajaxForm(function() {
          return alert("Thank you for your comment!");
        });
      });
    };
    Router.prototype.error404 = function(path) {
      return this.layout("#tpl-404", {
        path: path
      });
    };
    return Router;
  })();
  $(function() {
    window.routes = new Router();
    Backbone.history.start({
      pushState: false,
      root: "/"
    });
    $("body").on("click", "a#login-btn", function(e) {
      var credentials;
      credentials = {
        name: $("#login-name").val(),
        password: $("#login-password").val()
      };
      return $.ajax({
        type: 'post',
        url: '/login',
        data: credentials,
        success: (function(res) {
          return window.routes.session(function(s) {
            $("body div#navigation").html(_.template($("#navbar-tpl").html(), {
              session: s
            }));
            return sensordb.show_alert(".alert-success", ["Logged in successfully"]);
          });
        }),
        error: function(errors) {
          return sensordb.show_errors(errors);
        }
      });
    });
    return $("body").on("click", "a#logout-btn", function(e) {
      return $.ajax({
        type: 'post',
        url: '/logout',
        success: function() {
          lscache.flush();
          return window.routes.session(function(s) {
            console.log(s);
            $("body div#navigation").html(_.template($("#navbar-tpl").html(), {
              session: s
            }));
            return sensordb.show_alert(".alert-success", ["Logged out successfully"]);
          });
        }
      });
    });
  });
}).call(this);
