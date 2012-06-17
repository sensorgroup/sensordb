(function() {
  'use strict';
  var SDB, apply_tooltips;
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
    user = $routeParams['username'];
    $scope.hide_metadata = false;
    $scope.user = user;
    (lscache.get(SDB.SDB_SESSION_NAME) || {})[user] || $resource('/session', (user ? {
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
    return apply_selection_filters = function(session, selected_stream, selected_node, selected_exp) {
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
