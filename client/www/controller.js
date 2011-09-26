/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ***************************************
// Variables
// ***************************************
var reportId;
var pictureSource;
var destinationType;
var imageQuality = 50;

// to test on a device, you need to modify the IP for the local instance of the API service
// var apiUrl = "http://192.168.0.4:8080/api/";

// use this address when running on the Android emulator
var apiUrl = "http://10.0.2.2:8080/api/";

function getApiUrl(path) {
    return apiUrl + path;
}

// ***************************************
// Initialization
// ***************************************

// In order to use the camera hardware, we need to hook into the device
document.addEventListener("deviceready", onDeviceReady, false);

function onDeviceReady() {
    pictureSource = navigator.camera.PictureSourceType;
    destinationType = navigator.camera.DestinationType;
}

// ***************************************
// Create New - Purpose
// ***************************************

$('#create-new-purpose').live('pagecreate', function(event) {
    $("#create-new-purpose-form").validate({
        submitHandler : function(form) {
            console.log("Create New Purpose Submitted");
            submitCreateNewReportForm();
        }
    });
});

function submitCreateNewReportForm() {
    $.mobile.showPageLoadingMsg();

    var formData = $("#create-new-purpose-form").serialize();

    $.ajax({
        type : 'POST',
        url : getApiUrl("reports"),
        cache : false,
        data : formData,
        success : onCreateReportSuccess,
        error : onCreateReportError
    });

    return false;
}

function onCreateReportSuccess(data, status) {
    $.mobile.hidePageLoadingMsg();
    reportId = $.trim(data);
    $.mobile.changePage($('#create-new-expenses'));
}

function onCreateReportError(data, status) {
    $.mobile.hidePageLoadingMsg();
    alert('Error creating report: ' + status);
}

// ***************************************
// Create New - Expenses
// ***************************************

$('#create-new-expenses').live('pagecreate', function(event) {
    $("#create-new-expenses-next").click(function() {

        // collect the ids for the selected charges
        var a = [];
        $('#charges-list :checked').each(function() {
            a.push(Number($(this).val()));
        });

        // $.mobile.showPageLoadingMsg();

        var chargeIds = {
            chargeIds : a
        };

        var postData = $.toJSON(chargeIds);
        alert(postData);
        var url = getApiUrl('reports/' + reportId + '/expenses');

        $.ajax({
            type : 'POST',
            url : url,
            cache : false,
            dataType : 'json',
            data : postData,
            contentType : 'application/json; charset=utf-8',
            success : onAssociateExpensesSuccess,
            error : onAssociateExpensesError
        });

        return false;
    });
});

$('#create-new-expenses').live('pageshow', function(event, ui) {

    $.mobile.showPageLoadingMsg();

    // dynamically create the list of checkboxes
    $.getJSON(getApiUrl("reports/eligible-charges"), function(data) {
        var content = '<fieldset data-role="controlgroup">';
        $.each(data, function(i, charge) {
            var cbId = 'checkbox-' + i;
            content += '<input type="checkbox" name="' + cbId + '" id="' + cbId + '" value="' + charge.id + '" class="custom" />';
            content += '<label for="' + cbId + '">' + charge.date + ' - ' + charge.amount + ' - ' + charge.merchant + '</label>';
        });
        content += '</fieldset>';

        // set the content and trigger the create event to refresh and format the list properly
        $('#charges-list').html(content).trigger('create');

        $.mobile.hidePageLoadingMsg();
    });
});

function onAssociateExpensesSuccess(data, status) {
    alert(data);
    // $.mobile.hidePageLoadingMsg();
    // TODO: a collection of expenses is returned
    // $.mobile.changePage($("#create-new-add-receipt"));
}

function onAssociateExpensesError(data, status) {
    $.mobile.hidePageLoadingMsg();
    alert("Error associating expenses: " + status);
}

// ***************************************
// Create New - Add Receipt
// ***************************************

$('#create-new-add-receipt').live('pagecreate', function(event) {

    $("#create-new-add-receipt-capture-photo").click(function() {
        navigator.camera.getPicture(onPhotoCaptureSuccess, onPhotoCaptureFail, {
            quality : imageQuality,
            destinationType : destinationType.FILE_URI
        });
        return false;
    });

    $("#create-new-expenses-next").click(function() {
        $.mobile.changePage($("#create-new-confirm"));
        return false;
    });
});

function onPhotoCaptureSuccess(imageURI) {
    var image = document.getElementById('receiptImage');
    image.src = imageURI;
    $.mobile.changePage($("#create-new-review-receipt"));
}

function onPhotoCaptureFail(message) {
    alert('Failed to capture image [' + message + ']');
}

// ***************************************
// Create New - Review Receipt
// ***************************************

$('#create-new-review-receipt').live('pagecreate', function(event) {
    $("#create-new-review-receipt-next").click(function() {
        $.mobile.changePage($("#create-new-confirm"));
        return false;
    });
});

// ***************************************
// Review Status
// ***************************************

$('#review-status').live('pageshow', function(event, ui) {
    $.mobile.showPageLoadingMsg();

    // dynamically create the list of open reports
    $.getJSON(getApiUrl("reports"), function(data) {
        var content = '';
        $.each(data, function(i, expenseReport) {
            if (expenseReport.purpose != null) {
                content += '<li><a href="#create-new-confirm">' + expenseReport.purpose + '</a></li>';
            }
        });

        // set the new content and refresh the UI
        $('#reports-list').append(content).listview('refresh');

        $.mobile.hidePageLoadingMsg();
    });
});