this.ESA = this.ESA || {};
this.ESA.Utils = {
    jsonGet: function(path, tag, callback) {
        return $.getJSON(path+tag, function (data) {
            if (callback) callback(data);
        })
    },

    variance: function(a, b) {
        return (((b - a) / a) * 100).toFixed(1);
    },

    formatWoW: function(a, b) {
        var out = this.variance(a, b);
        if (!isFinite(out)) return '';
        if (out > 0) return "+" + out + "%";
        return out + "%";
    }
}

