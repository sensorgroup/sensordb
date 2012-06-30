(function() {
  'use strict';
  var SDB, apply_tooltips;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  $(function() {
    return $.ajaxSetup({
      beforeSend: function() {
        return $('.ajax-loading img').show();
      },
      complete: function() {
        return $('.ajax-loading img').hide();
      }
    });
  });
  SDB = (function() {
    function SDB() {}
    SDB.INVALIDATE_SESSION = 'invalidate-session';
    SDB.LOGOUT = 'logout';
    SDB.SESSION_INFO = 'session-info';
    SDB.SDB_SESSION_NAME = "sdb";
    SDB.ERROR_ALERT_MESSAGE = "error-alert-msg";
    SDB.SUCCESS_ALERT_MESSAGE = "success-alert-msg";
    SDB.CLEAR_ALERT_MESSAGE = "CLEAR_ALERT_MESSAGE";
    SDB.USER_NAME = "username";
    return SDB;
  })();
  this.module("sensordb", function() {
    this.LineChart = (function() {
      function LineChart(location, unit, data_provider_func) {
        var end_date, options, place_holder, plot, start_date, to_plot;
        this.location = location;
        this.unit = unit;
        this.data_provider_func = data_provider_func;
        this.elem = $(location);
        options = {
          legend: {
            position: 'nw',
            backgroundOpacity: 0.5,
            backgroundColor: null
          },
          series: {
            lines: {
              lineWidth: 1
            }
          },
          xaxis: {
            mode: 'time',
            localTimezone: false
          },
          yaxes: [
            {
              position: "left",
              axisLabel: unit,
              axisLabelUseCanvas: true
            }
          ],
          grid: {
            show: true,
            borderWidth: 1,
            borderColor: "#ccc"
          },
          selection: {
            mode: "xy"
          }
        };
        to_plot = _.map(req_res, function(rr) {
          return {
            shadowSize: 1,
            data: rr._res.data,
            yaxis: 1
          };
        });
        place_holder = this.elem.find('.flot');
        place_holder.unbind("plotselected").bind("plotselected", __bind(function(event, ranges) {
          var end_date, plot, start_date;
          plot = $.plot(place_holder, to_plot, $.extend(true, {}, options, {
            xaxis: {
              min: ranges.xaxis.from,
              max: ranges.xaxis.to,
              yaxis: {
                min: (ranges.yaxis === void 0 ? 0 : ranges.yaxis.from),
                max: (ranges.yaxis === void 0 ? 0 : ranges.yaxis.to)
              }
            },
            y2axis: {
              min: (ranges.y2axis === void 0 ? 0 : ranges.y2axis.from),
              max: (ranges.y2axis === void 0 ? 0 : ranges.y2axis.to),
              axisLabel: (right_axis_unit.length > 0 ? right_axis_unit[0] : void 0)
            }
          }));
          this.elem.find(".caption .reset-zoom").unbind("click").click(__bind(function() {
            return this.plot(conf, req_res);
          }, this));
          start_date = parseInt((plot.getAxes()['xaxis']['min']).toFixed(0));
          end_date = parseInt((plot.getAxes()['xaxis']['max']).toFixed(0));
          this.elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format());
          return this.elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format());
        }, this));
        plot = $.plot(place_holder, to_plot, options);
        start_date = plot.getAxes()['xaxis']['min'];
        end_date = plot.getAxes()['xaxis']['max'];
        this.elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format());
        this.elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format());
        this.elem.find(".caption").show();
      }
      return LineChart;
    })();
    return this.Utils = (function() {
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
      return Utils;
    })();
  });
  apply_tooltips = function() {
    $("a [rel=tooltip]").tooltip();
    return $("a [rel=tooltip]").click(function() {
      return $(this).tooltip('hide');
    });
  };
  window.LoginCtrl = function($scope, $location, $routeParams) {};
  window.LogoutCtrl = function($scope, $location, $routeParams) {};
  window.RegistrationCtrl = function($scope, $location, $routeParams, $timeout) {
    return $("body textarea").cleditor(sensordb.Utils.editor_config);
  };
  window.HomeCtrl = function($scope, $location, $routeParams, $cookies, $resource, $timeout) {
    return $scope.users = $resource("/users").query(function() {
      return $timeout(apply_tooltips, 500);
    });
  };
  window.AnalysisCtrl = function($scope, $location, $routeParams) {};
  window.DataPageCtrl = function($scope, $rootScope, $location, $routeParams, $resource) {
    var apply_selection_filters, user;
    $scope.plot_data_stream_chart = function() {
      var elem, end_date, options, place_holder, plot, start_date, to_plot;
      options = {
        series: {
          lines: {
            lineWidth: 1
          }
        },
        xaxis: {
          mode: 'time',
          localTimezone: false
        },
        grid: {
          show: true,
          borderWidth: 1,
          borderColor: "#ccc"
        },
        selection: {
          mode: "xy"
        }
      };
      to_plot = [
        {
          shadowSize: 2,
          data: [[-373597200000, 315.71], [-370918800000, 317.45], [-368326800000, 317.50]],
          yaxis: 1
        }
      ];
      elem = $("#stream-chart");
      place_holder = elem.find('.flot');
      place_holder.unbind("plotselected").bind("plotselected", __bind(function(event, ranges) {
        var end_date, plot, start_date;
        plot = $.plot(place_holder, to_plot, $.extend(true, {}, options, {
          xaxis: {
            min: ranges.xaxis.from,
            max: ranges.xaxis.to
          },
          yaxis: {
            min: (ranges.yaxis === void 0 ? 0 : ranges.yaxis.from),
            max: (ranges.yaxis === void 0 ? 0 : ranges.yaxis.to)
          }
        }));
        elem.find(".caption .reset-zoom").unbind("click").click(__bind(function() {
          return $scope.plot_data_stream_chart();
        }, this));
        start_date = parseInt((plot.getAxes()['xaxis']['min']).toFixed(0));
        end_date = parseInt((plot.getAxes()['xaxis']['max']).toFixed(0));
        elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format());
        return elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format());
      }, this));
      plot = $.plot(place_holder, to_plot, options);
      start_date = plot.getAxes()['xaxis']['min'];
      end_date = plot.getAxes()['xaxis']['max'];
      elem.find(".caption .from_timestamp").html(new Date(start_date).utc_format());
      elem.find(".caption .to_timestamp").html(new Date(end_date).utc_format());
      return elem.find(".caption").show();
    };
    user = $routeParams['username'];
    $scope.hide_metadata = false;
    $scope.user = user;
    $resource('/measurements').query(function(measurements) {
      return $scope.measurements = _.reduce(measurements, (function(sum, item) {
        sum[item._id] = item;
        return sum;
      }), {});
    });
    $resource('/session', (user ? {
      'user': user
    } : {})).get(function(session) {
      var node_ids, selection_id;
      $rootScope.$broadcast(SDB.SESSION_INFO, session);
      $scope.session = session;
      $scope.experiments = _.reduce(session.experiments, (function(sum, item) {
        sum[item._id] = item;
        return sum;
      }), {});
      $scope.nodes = _.reduce(session.nodes, (function(sum, item) {
        sum[item._id] = item;
        return sum;
      }), {});
      $scope.streams = _.reduce(session.streams, (function(sum, item) {
        sum[item._id] = item;
        return sum;
      }), {});
      $scope.experiment_names = _.sortBy(_.map(session.experiments, function(e) {
        return e.name;
      }), function(x) {
        return x;
      });
      $scope.node_names = _.sortBy(_.uniq(_.map(session.nodes, function(n) {
        return n.name;
      })), function(x) {
        return x;
      });
      $scope.stream_names = _.sortBy(_.uniq(_.map(session.streams, function(s) {
        return s.name;
      })), function(x) {
        return x;
      });
      selection_id = $routeParams.selection_id;
      if (selection_id) {
        $scope.selection_stream = $scope.streams[selection_id];
        $scope.selection_node = $scope.nodes[selection_id];
        $scope.selection_experiment = $scope.experiments[selection_id];
        node_ids = {};
        $scope.selection_experiment_nodes = _.reduce(session.nodes, (function(sum, v) {
          if (v.eid === selection_id) {
            sum += 1;
            node_ids[v._id] = 1;
          }
          return sum;
        }), 0);
        $scope.selection_experiment_streams = _.reduce(session.streams, (function(sum, v) {
          if (node_ids[v.nid]) {
            sum += 1;
          }
          return sum;
        }), 0);
      }
      if ($scope.selection_stream) {
        $scope.selection_sid = selection_id;
        $scope.selection_nid = $scope.selection_stream.nid;
        $scope.selection_eid = $scope.nodes[$scope.selection_nid].eid;
      }
      if ($scope.selection_node) {
        $scope.selection_sid = void 0;
        $scope.selection_nid = selection_id;
        $scope.selection_eid = $scope.selection_node.eid;
        $scope.selection_node_streams = _.reduce(session.streams, (function(sum, v) {
          if (v.nid === selection_id) {
            sum += 1;
          }
          return sum;
        }), 0);
      }
      if ($scope.selection_experiment) {
        $scope.selection_sid = void 0;
        $scope.selection_nid = void 0;
        $scope.selection_eid = selection_id;
      }
      return apply_selection_filters($scope.session);
    });
    $("body").on("click", function(e) {
      var src;
      src = $(e.srcElement);
      if (src.attr("id") === "show_hide_metadata") {
        return $scope.hide_metadata = !$scope.hide_metadata;
      }
    });
    $(".filter-selector").on('click', function(e) {
      var newSelection, oldSelection, parent, selected_exp, selected_node, selected_stream, src;
      src = $(e.srcElement);
      newSelection = src.text();
      parent = src.parents(".btn-group").find(".btn-label");
      oldSelection = parent.text();
      if (oldSelection !== newSelection) {
        parent.text(newSelection);
        selected_stream = _.filter([$("#stream-filter .btn-label").text()], function(i) {
          return i !== "All Streams";
        })[0];
        selected_node = _.filter([$("#node-filter .btn-label").text()], function(i) {
          return i !== "All Nodes";
        })[0];
        selected_exp = _.filter([$("#experiment-filter .btn-label").text()], function(i) {
          return i !== "Choose an Experiment";
        })[0];
        return apply_selection_filters(selected_stream, selected_node, selected_exp, $scope.session);
      }
    });
    apply_selection_filters = function(session, selected_stream, selected_node, selected_exp) {
      var exp, experiment_ids, experiment_names, experiments, filtered_rows, node_eids, node_exp, node_ids, node_names, nodes, seen_exps, seen_nodes, stream_names, stream_nids, stream_node_exp, streams;
      if (selected_exp || selected_node || selected_stream) {
        streams = selected_stream ? _.filter(session.streams, function(s) {
          return s.name === selected_stream;
        }) : session.streams;
        stream_nids = _.map(streams, function(s) {
          return s.nid;
        });
        nodes = selected_node ? _.filter(session.nodes, function(n) {
          return n.name === selected_node;
        }) : (selected_stream ? _.filter(session.nodes, function(n) {
          return _.indexOf(stream_nids, n._id) >= 0;
        }) : session.nodes);
        node_eids = _.map(nodes, function(n) {
          return n.eid;
        });
        experiments = selected_exp ? _.filter(session.experiments, function(e) {
          return e.name === selected_exp;
        }) : _.filter(session.experiments, function(e) {
          return _.indexOf(node_eids, e._id) >= 0;
        });
        experiment_ids = _.map(experiments, function(i) {
          return i._id;
        });
        nodes = _.filter(nodes, function(n) {
          return _.indexOf(experiment_ids, n.eid) >= 0;
        });
        node_ids = _.map(nodes, function(n) {
          return n._id;
        });
        streams = _.filter(streams, function(s) {
          return _.indexOf(node_ids, s.nid) >= 0;
        });
        experiment_names = _.sortBy(_.map(experiments, function(e) {
          return e.name;
        }), function(x) {
          return x;
        });
        node_names = _.sortBy(_.uniq(_.map(nodes, function(n) {
          return n.name;
        })), function(x) {
          return x;
        });
        return stream_names = _.sortBy(_.uniq(_.map(streams, function(s) {
          return s.name;
        })), function(x) {
          return x;
        });
      } else {
        seen_nodes = {};
        seen_exps = {};
        filtered_rows = [];
        stream_node_exp = _.reduce($scope.streams, (function(sum, v) {
          var exp_id;
          exp_id = $scope.nodes[v.nid].eid;
          sum.push({
            s: v._id,
            n: v.nid,
            e: exp_id
          });
          seen_nodes[v.nid] = 1;
          seen_exps[exp_id] = 1;
          return sum;
        }), filtered_rows);
        node_exp = _.reduce(_.difference(_.keys($scope.nodes), _.keys(seen_nodes)) || [], (function(sum, v) {
          var node;
          node = $scope.nodes[v];
          seen_exps[node.eid] = 1;
          seen_nodes[node._id] = 1;
          sum.push({
            n: node._id,
            e: node.eid
          });
          return sum;
        }), filtered_rows);
        exp = _.reduce(_.difference(_.keys($scope.experiments), _.keys(seen_exps)), (function(sum, eid) {
          sum.push({
            e: eid
          });
          return sum;
        }), filtered_rows);
        return $scope.filtered_rows = _.union(stream_node_exp, node_exp, exp);
      }
    };
    $scope.period = -1;
    return $scope.set_period = function(period) {
      var all_streams;
      if (period === $scope.period) {
        return;
      }
      $scope.period = period;
      return all_streams = _($scope.filtered_rows).map(function(v) {
        return v.s;
      }).filter(function(v) {
        return v !== void 0;
      });
    };
  };
  window.ExperimentCreateCtrl = function($scope, $location, $routeParams, $timeout) {
    return $("body textarea").cleditor(sensordb.Utils.editor_config);
  };
  window.NodeCreateCtrl = function($scope, $location, $routeParams, $timeout) {
    return $("body textarea").cleditor(sensordb.Utils.editor_config);
  };
  window.StreamCreateCtrl = function($scope, $location, $routeParams, $timeout) {
    return $("body textarea").cleditor(sensordb.Utils.editor_config);
  };
  window.DataExplorerCtrl = function($scope) {};
  window.Err404Ctrl = function($scope) {
    return $scope.url = window.location.href;
  };
  window.HeaderCtrl = function($scope, $rootScope, $cookies, $timeout, $resource) {
    $resource('/session').get(function(session) {
      return $rootScope.$broadcast(SDB.SESSION_INFO, session);
    });
    $scope.$on(SDB.CLEAR_ALERT_MESSAGE, function() {
      return $timeout((function() {
        return $scope.success_messages = $scope.error_messages = void 0;
      }), 1500);
    });
    $scope.$on(SDB.ERROR_ALERT_MESSAGE, function(scopy, messages) {
      $scope.error_messages = messages;
      $scope.success_messages = void 0;
      $("body").scrollTop(0);
      $scope.$broadcast(SDB.CLEAR_ALERT_MESSAGE);
      return $scope.$apply();
    });
    $scope.$on(SDB.SUCCESS_ALERT_MESSAGE, function(scopy, messages) {
      $scope.success_messages = messages;
      $scope.error_messages = void 0;
      $("body").scrollTop(0);
      $scope.$broadcast(SDB.CLEAR_ALERT_MESSAGE);
      return $scope.$apply();
    });
    $scope.$on(SDB.LOGOUT, function() {
      $scope.loggedIn = false;
      $rootScope.$broadcast(SDB.SUCCESS_ALERT_MESSAGE, ["Logged out Successfully"]);
      $("#login-name").val("Username");
      $("#login-password").val("password");
      $scope.errors = void 0;
      return $scope.$apply();
    });
    $scope.$on(SDB.SESSION_INFO, function(msg_name, session) {
      var cache, _ref, _ref2, _ref3, _ref4;
      if (!(session != null ? (_ref = session.user) != null ? _ref.name : void 0 : void 0)) {
        return;
      }
      cache = lscache.get(SDB.SDB_SESSION_NAME) || {};
      cache[session.user.name] = session;
      if ((session != null ? (_ref2 = session.user) != null ? _ref2.email : void 0 : void 0)) {
        $scope.username = session != null ? (_ref3 = session.user) != null ? _ref3.name : void 0 : void 0;
        lscache.set(SDB.USER_NAME, session != null ? (_ref4 = session.user) != null ? _ref4.name : void 0 : void 0);
      }
      return $scope.loggedIn = lscache.get(SDB.USER_NAME);
    });
    $scope.$on(SDB.INVALIDATE_SESSION, function() {
      return lscache.flush();
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
          var session_info;
          session_info = jQuery.parseJSON(res);
          $rootScope.$broadcast(SDB.SESSION_INFO, session_info);
          return $rootScope.$broadcast(SDB.SUCCESS_ALERT_MESSAGE, ["Logged in Successfully"]);
        }),
        error: function(res) {
          return $rootScope.$broadcast(SDB.ERROR_ALERT_MESSAGE, jQuery.parseJSON(res.responseText)["errors"]);
        }
      });
    });
    return $("body").on("click", "a#logout-btn", function(e) {
      return $.ajax({
        type: 'post',
        url: '/logout',
        success: function() {
          $rootScope.$broadcast(SDB.INVALIDATE_SESSION);
          return $rootScope.$broadcast(SDB.LOGOUT);
        }
      });
    });
  };
  window.FooterCtrl = function($scope, $location) {
    return $scope.isHomePage = $location.url() === '/';
  };
}).call(this);
