<html>
<head>
<!-- Load Leaflet from CDN-->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/leaflet/1.0.0-beta.2/leaflet.css" />
  <script src="https://cdn.jsdelivr.net/leaflet/1.0.0-beta.2/leaflet.js"></script>
<script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet-providers/1.1.7/leaflet-providers.min.js"></script>
<!-- Load Esri Leaflet from CDN -->
  <script src="https://cdn.jsdelivr.net/leaflet.esri/2.0.0-beta.8/esri-leaflet.js"></script>
</head>
<body>

NLDI Services ${version}
<form name="QueryTypeForm">
	<label>Source</label>
    <select name="SourceType" property="source_type" size="1">
        <option selected="selected"> </option>
        <option value="comid">comid</option>
        <option value="wqp">wqp</option>
        <option value="huc12pp">huc12pp</option>
    </select>
    <label>identifier</label><input aria-label="ComID" property="comID" name="ComIDField"></input>
    <label>Query Type</label>
    <select name="QueryType" property="query_type" size="1">
        <option selected="selected"> </option>
        <option value="UM">Upstream Main</option>
        <option value="DM">Downstream Main</option>
        <option value="DD">Downstream with Diversions</option>
        <option value="UT">Upstream with Tributaries</option>
    </select>
    <label>distance</label><input property="distance" name="DistanceField">
    <button type="button" onclick="on_submit_action();">submit!</button>
</form>
<div id="map" style="width: 100%; height: 600px;"></div>

<script type="text/javascript">
var map = L.map('map');
L.esri.basemapLayer("Gray").addTo(map);
  L.esri.tiledMapLayer({
    url: "https://hydrology.esri.com/arcgis/rest/services/WorldHydroReferenceOverlay/MapServer"
  }).addTo(map);
map.setView([35.9908385, -78.9005222], 3);

var geojsonWqpMarkerOptions = {
    radius: 4,
    fillColor: "#ff7800",
    color: "#000",
    weight: 1,
    opacity: 1,
    fillOpacity: 0.8
};

var geojsonhuc12ppMarkerOptions = {
	    radius: 4,
	    fillColor: "#b2ff59",
	    color: "#000",
	    weight: 1,
	    opacity: 1,
	    fillOpacity: 0.8
	};

function onEachPointFeature(feature, layer) {
            var popupText = "Data Source: " + feature.properties.source
                + "<br>Data Source Name: " + feature.properties.sourceName
                + "<br>Station Name: " + feature.properties.name
                + "<br>Station ID: " + feature.properties.identifier
                + "<br>Station ComID: " + feature.properties.comid
                + "<br>Station Data: " + "<a href="+feature.properties.uri+">click for csv</a>"
                + "<br>Station Navigation Options: " + "<a href="+feature.properties.navigation+">click for options</a>";
            layer.bindPopup(popupText);
            }

function onEachLineFeature(feature, layer) {
            var popupText = "Data Source: NHD+"
                + "<br>Reach ComID: " + feature.properties.nhdplus_comid
            layer.bindPopup(popupText);
            }

function addPointDataToMap(data, map, markerOptions) {
    var pointLayer = L.geoJson(data, {
        onEachFeature: onEachPointFeature,
        pointToLayer: function (feature, latlng) {
        return L.circleMarker(latlng, markerOptions);
        }
        });
    pointLayer.addTo(map);
    map.fitBounds(L.geoJson(data).getBounds());
}

function addLineDataToMap(data, map) {

    var lineLayer = L.geoJson(data, {
        onEachFeature: onEachLineFeature
        });
    lineLayer.addTo(map);
    //map.fitBounds(L.geoJson(data).getBounds());
}

var nldiURL = "../";

function on_submit_action()
    {

    var f=document.getElementsByName("SourceType")[0];
    var e=document.getElementsByName("QueryType")[0];
        var c=document.getElementsByName("ComIDField")[0];
        var d=document.getElementsByName("DistanceField")[0];
        var wqpURL = nldiURL+f.value+"/"+c.value+"/navigate/"+e.value+"/wqp";
        var huc12ppURL = nldiURL+f.value+"/"+c.value+"/navigate/"+e.value+"/huc12pp";
        var nhdURL = nldiURL+f.value+"/"+c.value+"/navigate/"+e.value;
        console.log(d.value);
        console.log(wqpURL);
        //$.get(wqpURL, {}, function(data) { addPointDataToMap(data, map); };);
        console.log("getting sites");
        $.getJSON( wqpURL, {distance:d.value}, function(data) { addPointDataToMap(data, map, geojsonWqpMarkerOptions); });
        console.log("sites added, getting streams");
        $.getJSON( nhdURL, {distance:d.value}, function(data) { addLineDataToMap(data, map); });
        console.log("sites and stream added, getting huc12pp");
        $.getJSON( huc12ppURL, {distance:d.value}, function(data) { addPointDataToMap(data, map, geojsonhuc12ppMarkerOptions); });


    }


</script>
</body>
</html>