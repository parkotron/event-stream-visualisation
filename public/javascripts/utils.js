this.ESA = this.ESA || {};
this.ESA.Utils = {
    jsonGet: function(path, tag, callback) {
        return $.getJSON(path+tag, function (data) {
            if (callback) callback(data);
        })
    }
}

