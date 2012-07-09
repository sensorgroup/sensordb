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
      Utils.calc_std = function(sum, sumSq, count) {
        return Math.sqrt((sumSq - sum * sum / count) / (count - 1));
      };
      Utils.editor_config = {
        width: '700px',
        height: "250px",
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
    var selection_id, user;
    $scope.local_tz_offset = (new Date()).getTimezoneOffset() * 60;
    $scope.calc_std = sensordb.Utils.calc_std;
    selection_id = $routeParams.selection_id;
    $scope.plot_data_stream_chart = function() {
      var options, place_holder, plot, to_plot;
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
          shadowSize: 0,
          data: ($scope.agg_period === "raw" ? _.map($scope.data, function(d) {
            return [(d[0] - 2 * $scope.local_tz_offset) * 1000, d[1]];
          }) : _.map($scope.data, function(d) {
            return [((d[0] + d[1]) / 2) * 1000, d[7] / d[6]];
          })),
          yaxis: 1
        }
      ];
      place_holder = $('#flot');
      place_holder.unbind("plotselected").bind("plotselected", __bind(function(event, ranges) {
        var plot;
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
        $scope.plot_from = ranges.xaxis.from;
        $scope.plot_to = ranges.xaxis.to;
        return $scope.$apply();
      }, this));
      return plot = $.plot(place_holder, to_plot, options);
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
    $scope.set_agg_period = function(period) {
      return $resource("/data", {
        sid: selection_id,
        level: period
      }).get(function(data) {
        $scope.agg_period = period;
        $scope.table_time_format = (function() {
          switch (period) {
            case "raw":
              return 'dd MMM yyyy - HH:mm:ss';
            case "1-hour":
              return 'dd MMM yyyy - HHa';
            case "1-day":
              return 'dd MMM yyyy';
            case "1-month":
              return 'MMMM yyyy';
            case "1-year":
              return 'yyyy';
          }
        })();
        $scope.data = _.sortBy(data[selection_id], function(d) {
          return d[0];
        });
        $scope.plot_from = $scope.first_updated = ($scope.data[0][0] - $scope.local_tz_offset) * 1000;
        $scope.plot_to = $scope.last_updated = ($scope.data[$scope.data.length - 1][period === "raw" ? 0 : 1] - $scope.local_tz_offset) * 1000;
        return $scope.plot_data_stream_chart();
      });
    };
    $resource('/session', (user ? {
      'user': user
    } : {})).get(function(session) {
      var node_ids;
      $rootScope.$broadcast(SDB.SESSION_INFO, session);
      $scope.session = session;
      if (selection_id) {
        $scope.selection_stream = _.find($scope.session.streams, function(s) {
          return s._id === selection_id;
        });
        $scope.selection_node = _.find($scope.session.nodes, function(n) {
          return n._id === selection_id;
        });
        $scope.selection_experiment = _.find($scope.session.experiments, function(e) {
          return e._id === selection_id;
        });
      }
      if ($scope.selection_stream) {
        $scope.selection_node = _.find($scope.session.nodes, function(n) {
          return n._id === $scope.selection_stream.nid;
        });
        $scope.selection_experiment = _.find($scope.session.experiments, function(e) {
          return e._id === $scope.selection_node.eid;
        });
        $scope.set_agg_period("1-day");
      }
      if ($scope.selection_node) {
        $scope.selection_experiment = _.find($scope.session.experiments, function(e) {
          return e._id === $scope.selection_node.eid;
        });
        $scope.selection_node_streams = _.reduce(session.streams, (function(sum, v) {
          if (v.nid === selection_id) {
            sum += 1;
          }
          return sum;
        }), 0);
      }
      if ($scope.selection_experiment) {
        node_ids = {};
        $scope.selection_experiment_nodes = _.reduce(session.nodes, (function(sum, v) {
          if (v.eid === selection_id) {
            sum += 1;
            node_ids[v._id] = 1;
          }
          return sum;
        }), 0);
        return $scope.selection_experiment_streams = _.reduce(session.streams, (function(sum, v) {
          if (node_ids[v.nid]) {
            sum += 1;
          }
          return sum;
        }), 0);
      }
    });
    return $("body").on("click", function(e) {
      var src;
      src = $(e.srcElement);
      if (src.attr("id") === "show_hide_metadata") {
        return $scope.hide_metadata = !$scope.hide_metadata;
      }
    });
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
  window.DataExplorerCtrl = function($scope, $routeParams, $resource, $rootScope) {
    var apply_source_filter, user;
    $scope.user = user = $routeParams['username'];
    $scope.calc_std = sensordb.Utils.calc_std;
    $scope.local_tz_offset = (new Date()).getTimezoneOffset() * 60;
    $resource('/measurements').query(function(measurements) {
      return $scope.measurements = _.reduce(measurements, (function(sum, item) {
        sum[item._id] = item;
        return sum;
      }), {});
    });
    $resource('/session', (user ? {
      'user': user
    } : {})).get(function(session) {
      $rootScope.$broadcast(SDB.SESSION_INFO, session);
      $scope.session = session;
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
      return $scope.apply_filters();
    });
    $scope.apply_filters = function() {
      var filtering_results, source_filter, _ref;
      source_filter = apply_source_filter();
      filtering_results = source_filter;
      if (((_ref = source_filter.s) != null ? _ref.length : void 0) > 0) {
        return $resource("/data", {
          sid: JSON.stringify(source_filter.s),
          level: "1-year"
        }).get(function(summaries) {
          summaries = _.reduce(summaries, function(summary, values, key) {
            var count, max, maxTs, maxTsVal, min, minTs, minTsVal, sumSq, total;
            summary[key] = values.length > 1 ? (minTs = values[0][0], maxTs = values[values.length - 1][1], minTsVal = values[0][2], maxTsVal = values[values.length - 1][3], min = _.reduce(values, (function(sum, num) {
              return Math.min(sum, num[4]);
            }), values[0][4]), max = _.reduce(values, (function(sum, num) {
              return Math.max(sum, num[5]);
            }), values[0][5]), count = _.reduce(values, (function(sum, num) {
              return sum + num[6];
            }), 0), total = _.reduce(values, (function(sum, num) {
              return sum + num[7];
            }), 0), sumSq = _.reduce(values, (function(sum, num) {
              return sum + num[8];
            }), 0), [minTs, maxTs, minTsVal, maxTsVal, min, max, count, total, sumSq]) : values[0];
            return summary;
          }, {});
          console.log(summaries);
          filtering_results.data = summaries;
          return $scope.filtering_results = filtering_results;
        });
      } else {
        return $scope.filtering_results = filtering_results;
      }
    };
    return apply_source_filter = function() {
      var experiments_tmp, nodes_tmp, nodes_tmp_eid, nodes_tmp_id, stream_node_ids, streams_tmp;
      experiments_tmp = _.map(($scope.experiment_selector !== void 0 && $scope.experiment_selector !== "" ? _.filter($scope.session.experiments, function(item) {
        return item.name === $scope.experiment_selector;
      }) : $scope.session.experiments), function(e) {
        return e._id;
      });
      nodes_tmp = $scope.node_selector !== void 0 && $scope.node_selector !== "" ? _.filter($scope.session.nodes, function(item) {
        return item.name === $scope.node_selector;
      }) : $scope.session.nodes;
      streams_tmp = $scope.stream_selector !== void 0 && $scope.stream_selector !== "" ? _.filter($scope.session.streams, function(item) {
        return item.name === $scope.stream_selector;
      }) : $scope.session.streams;
      streams_tmp = $scope.measurement_selector !== void 0 && $scope.measurement_selector !== "" ? _.filter(streams_tmp, function(item) {
        return item.mid === $scope.measurement_selector;
      }) : streams_tmp;
      nodes_tmp = _.filter(nodes_tmp, function(n) {
        return _.include(experiments_tmp, n.eid);
      });
      stream_node_ids = _.map(streams_tmp, function(s) {
        return s.nid;
      });
      nodes_tmp = _.filter(nodes_tmp, function(n) {
        return _.include(stream_node_ids, n._id);
      });
      nodes_tmp_id = _.map(nodes_tmp, function(n) {
        return n._id;
      });
      streams_tmp = _.filter(streams_tmp, function(s) {
        return _.include(nodes_tmp_id, s.nid);
      });
      nodes_tmp_eid = _.map(nodes_tmp, function(n) {
        return n.eid;
      });
      experiments_tmp = _.filter(experiments_tmp, function(e) {
        return _.include(nodes_tmp_eid, e);
      });
      return {
        e: experiments_tmp,
        n: _.map(nodes_tmp, function(n) {
          return n._id;
        }),
        s: _.map(streams_tmp, function(s) {
          return s._id;
        })
      };
    };
  };
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
