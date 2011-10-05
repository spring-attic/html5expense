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
var expenseReport;
var pictureSource;
var destinationType;
var imageQuality = 50;

// to test on a device, you need to modify the IP for the local instance of the API service
//var apiUrl = "http://192.168.0.5:8080/api/";
var apiUrl = "http://html5expense.cloudfoundry.com/";

// use this address when running on the Android emulator
//var apiUrl = "http://10.0.2.2:8080/api/";

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

    var url = getApiUrl("reports");
    var formData = $("#create-new-purpose-form").serialize();

    $.ajax({
        type : 'POST',
        url : url,
        cache : false,
        data : formData,
        success : onCreateReportSuccess,
        error : onCreateReportError
    });

    return false;
}

function onCreateReportSuccess(data, textStatus, jqXHR) {
    $.mobile.hidePageLoadingMsg();
    expenseReport = {};
    expenseReport.id = $.trim(data);
    $.mobile.changePage($('#create-new-expenses'));
}

function onCreateReportError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('errorThrown: ' + errorThrown);
    console.log('textStatus: ' + textStatus);
    alert('Error creating report');
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

        var url = getApiUrl('reports/' + expenseReport.id + '/expenses');
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

    $.ajax({
        type : 'GET',
        url : url,
        cache : false,
        dataType : 'json',
        success : onFetchEligibleExpensesSuccess,
        error : onFetchEligibleExpensesError
    });

    return false;
});

function onAssociateExpensesSuccess(data, textStatus, jqXHR) {
    $.mobile.hidePageLoadingMsg();
    expenseReport.expenses = data;

    if (receiptsRequired(expenseReport.expenses)) {
        $.mobile.changePage($("#create-new-add-receipt"));
    } else {
        // if no receipts are required, then display the confirmation page
        $.mobile.changePage($("#create-new-confirm"));
    }
}

function onAssociateExpensesError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    alert("Error associating expenses");
}

function onFetchEligibleExpensesSuccess(data, textStatus, jqXHR) {
    if (data.length == 0) {
        $.mobile.hidePageLoadingMsg();
        $('#create-new-expenses-next').button('disable');
        alert("There are no available expenses!");
    } else {
        var content = '<fieldset data-role="controlgroup">';
        $.each(data, function(i, charge) {
            var cbId = 'checkbox-' + i;
            content += '<input type="checkbox" name="' + cbId + '" id="' + cbId + '" value="' + charge.id + '" class="custom" />';
            content += '<label for="' + cbId + '">' + formatDate(charge.date) + ' - $' + $.currency(charge.amount) + ' - ' + charge.merchant + '</label>';
        });
        content += '</fieldset>';
        // set the content and trigger the create event to refresh and format the list properly
        $('#charges-list').html(content).trigger('create');
        $('#create-new-expenses-next').button('enable');
        $.mobile.hidePageLoadingMsg();
    }
}

function onFetchEligibleExpensesError(jqXHR, textStatus, errorThrown) {
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

    $("#create-new-add-receipt-skip").click(function() {
        alert("Receipts are required in order to submit this expense report!");
        // TODO: determine if another receipt is required
        $.mobile.changePage($("#create-new-confirm"));
        return false;
    });
});

function onPhotoCaptureSuccess(imageURI) {
    var image = document.getElementById('receiptImage');
    image.src = imageURI;
    
    // TODO: upload receipt
    
//    $.mobile.showPageLoadingMsg();
//
//    var url = getApiUrl("reports/" + expenseReport.id + "/expenses/" + 1 + "/receipt");
//    var imageData;
//
//    $.ajax({
//        type : 'POST',
//        url : url,
//        cache : false,
//        data : imageData,
//        contentType : 'multipart/form-data',
//        processData: false,
//        success : onUploadReceiptPhotoSuccess,
//        error : onUploadReceiptPhotoError
//    });
    
    
    $.mobile.changePage($("#create-new-review-receipt"));
}

function onPhotoCaptureFail(message) {
    alert('Failed to capture image: ' + message);
}

// ***************************************
// Create New - Review Receipt
// ***************************************

$('#create-new-review-receipt').live('pagecreate', function(event) {
    $("#create-new-review-receipt-next").click(function() {
        // TODO: determine if another receipt is required
        $.mobile.changePage($("#create-new-confirm"));
        return false;
    });
});

// ***************************************
// Create New - Confirm
// ***************************************

$('#create-new-confirm').live('pagecreate', function(event) {
    $("#create-new-confirm-submit").click(function() {
        $.mobile.showPageLoadingMsg();

        var url = getApiUrl("reports/" + expenseReport.id);

        $.ajax({
            type : 'PUT',
            url : url,
            cache : false,
            success : onSubmitExpenseReportSuccess,
            error : onSubmitExpenseReportError
        });

        return false;
    });
});

$('#create-new-confirm').live('pageshow', function(event) {
    var content = '<li data-role="list-divider">Expenses</li>';
    $.each(expenseReport.expenses, function(i, expense) {
        content += '<li>';
        content += '<p class="ui-li-aside">$' + $.currency(expense.amount) + '</p>';
        content += '<h3>' + expense.merchant + '</h3>';
        content += '<p>' + expense.category + '</p>';
        content += '<p>' + formatDate(expense.date) + '</p>';
        content += '</li>';
    });

    $('#expenses-list').html(content).listview('refresh');
});

function onSubmitExpenseReportSuccess(data, textStatus, jqXHR) {
    $.mobile.hidePageLoadingMsg();
    if (data == true) {
        alert('Expense report submitted');
        $.mobile.changePage($("#home"));
    } else {
        alert('Receipts required! Expense report not submitted');
    }
}

function onSubmitExpenseReportError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    alert("Error submitted expense report");
}

// ***************************************
// Review Status
// ***************************************

$('#review-status').live('pageshow', function(event, ui) {
    $.mobile.showPageLoadingMsg();

    var url = getApiUrl("reports");

    $.ajax({
        type : 'GET',
        url : url,
        cache : false,
        dataType : 'json',
        success : onFetchOpenExpenseReportsSuccess,
        error : onFetchOpenExpenseReportsError
    });

    $('.expense-report-list-item').live('click', function() {
        var expenseReportId = $(this).jqmData('id');
        $('#review-status-details').jqmData('expenseReportId', expenseReportId);
        $.mobile.changePage('#review-status-details');
    });
});

function onFetchOpenExpenseReportsSuccess(data, textStatus, jqXHR) {
    if (data.length == 0) {
        $.mobile.hidePageLoadingMsg();
        $('#reports-list').html('').listview('refresh');
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

function onFetchOpenExpenseReportsError(jqXHR, textStatus, errorThrown) {
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

// ***************************************
// Utility Methods
// ***************************************

function formatDate(date) {
    var dateString = JSON.stringify(date);
    //TODO: is there a better way to do this?
    return dateString.substring(1, dateString.length-1).replace(/,/g, "-");
}

function receiptsRequired(expenses) {
    var result = false;
    $.each(expenses, function(i, expense) {
        if (expense.flag == "receiptRequired") {
            result = true;
        }
    });
    return result;
} 