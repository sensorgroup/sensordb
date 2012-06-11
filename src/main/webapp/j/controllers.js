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
      Utils.SessionFor = function($rootScope, user, callback) {
        var session, _ref;
        session = (lscache.get(SDB.SDB_SESSION_NAME) || {})[user];
        if ((session != null ? (_ref = session.user) != null ? _ref.name : void 0 : void 0)) {
          return callback && callback(session);
        } else {
          return $.ajax({
            type: 'get',
            url: '/session',
            data: (user ? {
              'user': user
            } : {}),
            success: function(res) {
              var msg;
              msg = jQuery.parseJSON(res);
              $rootScope.$broadcast(SDB.SESSION_INFO, msg);
              return callback && callback(msg);
            }
          });
        }
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
  window.DataPageCtrl = function($scope, $rootScope, $location, $routeParams) {
    var user;
    user = $routeParams['username'];
    $scope.user = user;
    return sensordb.Utils.SessionFor($rootScope, user, function(session) {
      $scope.experiments = session.experiments;
      $scope.nodes = session.nodes;
      $scope.streams = session.streams;
      $scope.experiment_names = _.uniq(_.map(session.experiments, function(e) {
        return e.name;
      }));
      $scope.node_names = _.uniq(_.map(session.nodes, function(e) {
        return e.name;
      }));
      $scope.stream_names = _.uniq(_.map(session.streams, function(e) {
        return e.name;
      }));
      return $(".filter-selector").on('click', function(e) {
        var newSelection, oldSelection, parent, src;
        src = $(e.srcElement);
        newSelection = src.text();
        parent = src.parents(".btn-group").find(".btn-label");
        oldSelection = parent.text();
        if (oldSelection !== newSelection) {
          return parent.text(newSelection);
        }
      });
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
  window.Err404Ctrl = function($scope) {
    return $scope.url = window.location.href;
  };
  window.HeaderCtrl = function($scope, $rootScope, $cookies, $timeout) {
    sensordb.Utils.SessionFor($rootScope);
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
      $scope.loggedIn = lscache.get(SDB.USER_NAME);
      return $scope.$apply();
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
