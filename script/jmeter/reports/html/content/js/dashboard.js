/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header row to statistics table grouping metrics by category
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";

    var headers = [
        { colSpan: 1, text: "Requests" },
        { colSpan: 3, text: "Executions" },
        { colSpan: 7, text: "Response Times (ms)" },
        { colSpan: 1, text: "Throughput" },
        { colSpan: 2, text: "Network (KB/sec)" }
    ];

    headers.forEach(function(config) {
        var cell = document.createElement('th');
        cell.setAttribute("data-sorter", false);
        cell.colSpan = config.colSpan;
        cell.innerHTML = config.text;
        newRow.appendChild(cell);
    });
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 89.78697437035393, "KoPercent": 10.213025629646067};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    var apdexFormatter = function(index, item) {
        if (index === 0) {
            return item.toFixed(3);
        }
        if (index === 1 || index === 2) {
            return formatDuration(item);
        }
        return item;
    };

    var apdexInfo = {
        supportsControllersDiscrimination: true,
        overall: { data: [0.8787307222900255, 500, 1500, "Total"], isController: false },
        titles: ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"],
        items: [
            { data: [0.4335691823899371, 500, 1500, "GET /api/posts/1"], isController: false },
            { data: [0.8772228033472803, 500, 1500, "GET /api/items"], isController: false },
            { data: [0.8795798104022546, 500, 1500, "GET /api/posts"], isController: false },
            { data: [0.9417303644621691, 500, 1500, "GET /api/boards"], isController: false }
        ]
    };
    createTable($("#apdexTable"), apdexInfo, apdexFormatter, [[0, 0]], 3);

    // Create statistics table
    var statisticsFormatter = function(index, item) {
        if (index === 3) {
            return item.toFixed(2) + '%';
        }
        if (index >= 4 && index <= 13 && index !== 6) {
            return item.toFixed(2);
        }
        return item;
    };

    var statisticsInfo = {
        supportsControllersDiscrimination: true,
        overall: { data: ["Total", 18026, 1841, 10.213025629646067, 121.97570176411779, 3, 1126, 85.0, 230.0, 386.0, 702.0, 602.7754556094299, 1608.1968551454606, 67.08799871467981], isController: false },
        titles: ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"],
        items: [
            { data: ["GET /api/posts/1", 1272, 384, 30.18867924528302, 433.1713836477988, 3, 1126, 516.5, 743.7, 823.3499999999999, 1004.3499999999999, 42.551767972435016, 61.18933571404676, 3.539185595289867], isController: false },
            { data: ["GET /api/items", 3824, 466, 12.186192468619247, 143.7609832635985, 3, 559, 135.0, 237.0, 281.0, 400.5, 127.87159337903361, 616.4842313158334, 14.8037143663267], isController: false },
            { data: ["GET /api/posts", 3903, 465, 11.91391237509608, 140.8565206251605, 3, 729, 134.0, 234.0, 277.0, 372.8000000000002, 130.653098115355, 497.744716958039, 15.17262923459311], isController: false },
            { data: ["GET /api/boards", 9027, 526, 5.826963553783095, 60.73280159521444, 3, 440, 59.0, 116.0, 143.0, 222.71999999999935, 302.17922538747365, 433.79992881322596, 33.62610703017775], isController: false }
        ]
    };
    createTable($("#statisticsTable"), statisticsInfo, statisticsFormatter, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    var errorsFormatter = function(index, item) {
        if (index === 2 || index === 3) {
            return item.toFixed(2) + '%';
        }
        return item;
    };

    var errorsInfo = {
        supportsControllersDiscrimination: false,
        titles: ["Type of error", "Number of errors", "% in errors", "% in all samples"],
        items: [
            { data: ["Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 1841, 100.0, 10.213025629646067], isController: false }
        ]
    };
    createTable($("#errorsTable"), errorsInfo, errorsFormatter, [[1, 1]]);

    // Create top5 errors by sampler
    var top5ErrorsInfo = {
        supportsControllersDiscrimination: false,
        overall: { data: ["Total", 18026, 1841, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 1841, "", "", "", "", "", "", "", ""], isController: false },
        titles: ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"],
        items: [
            { data: ["GET /api/posts/1", 1272, 384, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 384, "", "", "", "", "", "", "", ""], isController: false },
            { data: ["GET /api/items", 3824, 466, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 466, "", "", "", "", "", "", "", ""], isController: false },
            { data: ["GET /api/posts", 3903, 465, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 465, "", "", "", "", "", "", "", ""], isController: false },
            { data: ["GET /api/boards", 9027, 526, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 526, "", "", "", "", "", "", "", ""], isController: false }
        ]
    };
    createTable($("#top5ErrorsBySamplerTable"), top5ErrorsInfo, function(index, item) { return item; }, [[0, 0]], 0);

});
