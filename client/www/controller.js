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
// var apiUrl = "http://192.168.0.8:8080/api/";

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
        $.mobile.showPageLoadingMsg();

        // collect the ids for the selected charges
        var arrayIds = [];
        $('#charges-list :checked').each(function() {
            arrayIds.push(Number($(this).val()));
        });

        var url = getApiUrl('reports/' + reportId + '/expenses');
        var postData = $.toJSON({
            chargeIds : arrayIds
        });

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

    var url = getApiUrl("reports/eligible-charges");
    var data;

    $.ajax({
        type : 'GET',
        url : url,
        cache : false,
        dataType : 'json',
        data : data,
        success : onFetchEligibleExpensesSuccess,
        error : onFetchEligibleExpensesError
    });
});

function onAssociateExpensesSuccess(data, status) {
    $.mobile.hidePageLoadingMsg();
    // TODO: a collection of expenses is returned
    $.mobile.changePage($("#create-new-add-receipt"));
}

function onAssociateExpensesError(data, status) {
    $.mobile.hidePageLoadingMsg();
    alert("Error associating expenses");
}

function onFetchEligibleExpensesSuccess(data, status) {
    if (data.length == 0) {
        $.mobile.hidePageLoadingMsg();
        $('#create-new-expenses-next').button('disable');
        alert("There are no available expenses!");
    } else {
        var content = '<fieldset data-role="controlgroup">';
        $.each(data, function(i, charge) {
            var cbId = 'checkbox-' + i;
            content += '<input type="checkbox" name="' + cbId + '" id="' + cbId + '" value="' + charge.id + '" class="custom" />';
            content += '<label for="' + cbId + '">' + charge.date + ' - ' + charge.amount + ' - ' + charge.merchant + '</label>';
        });
        content += '</fieldset>';
        // set the content and trigger the create event to refresh and format the list properly
        $('#charges-list').html(content).trigger('create');
        $('#create-new-expenses-next').button('enable');
        $.mobile.hidePageLoadingMsg();
    }
}

function onFetchEligibleExpensesError(data, status) {
    $.mobile.hidePageLoadingMsg();
    alert("Error fetching eligible expenses");
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

    var url = getApiUrl("reports");
    var data;

    $.ajax({
        type : 'GET',
        url : url,
        cache : false,
        dataType : 'json',
        data : data,
        success : onFetchOpenExpenseReportsSuccess,
        error : onFetchOpenExpenseReportsError
    });

    $('.expense-report-list-item').live('click', function() {
        var expenseReportId = $(this).jqmData('id');
        $('#review-status-details').jqmData('expenseReportId', expenseReportId);
        $.mobile.changePage('#review-status-details');
    });
});

function onFetchOpenExpenseReportsSuccess(data, status) {
    if (data.length == 0) {
        $.mobile.hidePageLoadingMsg();
        alert("There are no open expense reports");
    } else {
        var content = '';
        $.each(data, function(i, expenseReport) {
            if (expenseReport.purpose != null) {
                content += '<li data-id=' + expenseReport.id + ' class="expense-report-list-item"><a href="#">';
                content += '<p class="ui-li-count">' + expenseReport.expenses.length + '</p>';
                content += '<h3>' + expenseReport.purpose + '</h3>';
                content += '<p>Status: ' + expenseReport.state + '</p>';
                content += '</a></li>';
            }

            // set the new content and refresh the UI
            $('#reports-list').html(content).listview('refresh');

            $.mobile.hidePageLoadingMsg();
        });
    }
}

function onFetchOpenExpenseReportsError(data, status) {
    $.mobile.hidePageLoadingMsg();
    alert("Error fetching open expense reports");
}

// ***************************************
// Review Status Details
// ***************************************

$('#review-status-details').live('pagebeforeshow', function(event, ui) {
    var expenseReportId = $('#review-status-details').jqmData('expenseReportId');
    if (expenseReportId != null) {
        $('#review-status-details-text').text("Details about expense report #" + expenseReportId + " here.");
    } else {
        alert("No expense report available to display");
    }
});