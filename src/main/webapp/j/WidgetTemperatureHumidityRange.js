(function() {
  var WidgetTemperatureHumidityRange;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) {
    for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; }
    function ctor() { this.constructor = child; }
    ctor.prototype = parent.prototype;
    child.prototype = new ctor;
    child.__super__ = parent.prototype;
    return child;
  };
  WidgetTemperatureHumidityRange = (function() {
    __extends(WidgetTemperatureHumidityRange, sensordb.Widget);
    function WidgetTemperatureHumidityRange(e) {
      this.plot = __bind(this.plot, this);      WidgetTemperatureHumidityRange.__super__.constructor.call(this, e);
    }
    WidgetTemperatureHumidityRange.prototype.sample_config = function() {
      return "Sample configuration goes here.";
    };
    WidgetTemperatureHumidityRange.prototype.html_template_name = function() {
      return "#tpl-flot-chart";
    };
    WidgetTemperatureHumidityRange.prototype.html_template_config_name = function() {
      return "#tpl-flot-chart-config";
    };
    WidgetTemperatureHumidityRange.prototype.process_config = function(c) {
      var all_feeds, catalog, initial_selection, left, right;
      initial_selection = _.union(c.initial_graph.left_axis, c.initial_graph.right_axis);
      all_feeds = _.union(c.feeds, _.map(initial_selection, function(x) {
        return "" + x.user + "/" + x.experiment + "/" + x.node + "/" + x.stream;
      }));
      this.catalog = catalog = window.db.catalog(all_feeds);
      if (c.allow_axis_selection) {
        this.elem.find(".in-widget-config").html(_.template($(this.html_template_config_name()).html(), {
          id: this.id,
          catalog: this.catalog,
          left_axis: c.initial_graph.left_axis,
          right_axis: c.initial_graph.right_axis
        }));
        this.elem.find(".in-widget-config .modal input:checkbox").bind('change', __bind(function() {
          var left_selection, right_selection;
          left_selection = [];
          this.elem.find(".in-widget-config .Left.modal input:checked").each(function() {
            var experiment, field, node, sid, stream, unit, user, _ref;
            sid = $(this).attr('feed-id');
            _ref = sensordb.Utils.find_in_catalog_by_stream_id(sid, catalog), user = _ref[0], experiment = _ref[1], node = _ref[2], stream = _ref[3], unit = _ref[4];
            field = {
              user: user,
              live: c.live,
              node: node,
              sid: sid,
              stream: stream,
              experiment: experiment,
              unit: unit,
              to_date: c.to_date,
              from_date: c.from_date
            };
            return left_selection.push(field);
          });
          right_selection = [];
          this.elem.find(".in-widget-config .Right.modal input:checked").each(function() {
            var experiment, field, node, sid, stream, unit, user, _ref;
            sid = $(this).attr('feed-id');
            _ref = sensordb.Utils.find_in_catalog_by_stream_id(sid, catalog), user = _ref[0], experiment = _ref[1], node = _ref[2], stream = _ref[3], unit = _ref[4];
            field = {
              user: user,
              live: c.live,
              node: node,
              sid: sid,
              stream: stream,
              experiment: experiment,
              unit: unit,
              to_date: c.to_date,
              from_date: c.from_date
            };
            return right_selection.push(field);
          });
          _.each({
            Left: left_selection,
            Right: right_selection
          }, function(selection, direction) {
            if (selection.length === 0) {
              return $(".in-widget-config ." + direction + ".modal input:checkbox").removeAttr("disabled");
            } else {
              return $(".in-widget-config ." + direction + ".modal input:checkbox[unit!='" + selection[0].unit + "']").attr('disabled', true);
            }
          });
          return this.prepare_and_plot(c, left_selection, right_selection);
        }, this));
      }
      this.elem.find(".widget-output").html(_.template($(this.html_template_name()).html(), {
        conf: c,
        id: this.id
      }));
      left = _.map(c.initial_graph.left_axis, __bind(function(i) {
        var _ref, _ref2, _ref3;
        return _.extend(i, (_ref = this.catalog[i.user]) != null ? (_ref2 = _ref[i.experiment]) != null ? (_ref3 = _ref2[i.node]) != null ? _ref3[i.stream] : void 0 : void 0 : void 0);
      }, this));
      right = _.map(c.initial_graph.right_axis, __bind(function(i) {
        var _ref, _ref2, _ref3;
        return _.extend(i, (_ref = this.catalog[i.user]) != null ? (_ref2 = _ref[i.experiment]) != null ? (_ref3 = _ref2[i.node]) != null ? _ref3[i.stream] : void 0 : void 0 : void 0);
      }, this));
      return this.prepare_and_plot(c, left, right);
    };
    WidgetTemperatureHumidityRange.prototype.prepare_and_plot = function(config, left, right) {
      var data_requests, grouped_data_req;
      data_requests = _.map({
        'left': left,
        'right': right
      }, function(feed, direction) {
        return _.map(feed, function(f) {
          return new sensordb.DataRequest({
            unit: f.unit,
            axis: direction
          }, f.user, f.experiment, f.node, f.stream, f.fields, true, config.live, f.from_timestamp, f.to_timestamp);
        });
      });
      grouped_data_req = new sensordb.GroupedDataRequest({}, _.flatten(data_requests), __bind(function(req_map) {
        return this.plot(config, req_map);
      }, this));
      return window.rm.add_grouped_data_request(grouped_data_req);
    };
    WidgetTemperatureHumidityRange.prototype.plot = function(conf, req_res) {
      var left_axis_unit, right_axis_unit;
      left_axis_unit = _.uniq(_.map(_.filter(req_res, function(rr) {
        return rr.local_info.axis === 'left';
      }), function(rr) {
        return rr.local_info.unit;
      }));
      right_axis_unit = _.uniq(_.map(_.filter(req_res, function(rr) {
        return rr.local_info.axis === 'right';
      }), function(rr) {
        return rr.local_info.unit;
      }));
      return _.each({
        'left': left_axis_unit,
        'right': right_axis_unit
      }, function(lbl, name) {
        if (lbl.length > 1) {
          return console.log("" + name + " axis can have only one unit * " + (lbl.join(",")) + " is not valid, first one will be used.");
        }
      });
    };
    return WidgetTemperatureHumidityRange;
  })();
  ws.registerWidget("WidgetTemperatureHumidityRange", WidgetTemperatureHumidityRange);
}).call(this);
