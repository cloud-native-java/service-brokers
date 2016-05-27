$(document).ready(function() {
    $("input:file").change(function() {
        var fileName = $(this).val();
        $("#fileName").val(fileName.split("\\").pop())
            .attr("disabled", null);
    });

    $.getJSON("/s3/resources", function(data) {
        var items = [];
        $.each(data, function(key, val) {
            var fileName = val.links[0].href;
            var iconType = "file";
            if (fileName.match("(pdf)")) {
                iconType = "file-pdf";
            } else if (fileName.match("(txt)")) {
                iconType = "file-text";
            }
            if (fileName.match("(jpg|jpeg|png|gif|bmp|svg|tif|tiff)")) {
                items.push("<a class='image-link' href='" + fileName + "' target='_blank'>" +
                    "<div class='img-container img-thumbnail' style=\"background-image: url('" + fileName + "');\">" +
                    "</div></a>");
            } else {
                items.push("<a class='image-link' href='" + fileName + "' target='_blank'>" +
                    "<div class='img-container img-thumbnail'><i class='fa fa-" + iconType + "-o fa-5x'>" +
                    "</i></div></a>");
            }
        });

        $("<div/>", {
            "class": "my-new-list",
            html: items.join("")
        }).appendTo("#images");
    });
});
