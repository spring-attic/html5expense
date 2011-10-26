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

// camera related values
var pictureSource;
var destinationType;
var imageQuality = 50;
var imageWidth = 300;

// oauth
var clientId = '09e749d8309f4044';
var clientSecret = '189309492722aa5a';


// Use the following URL to test against the CloudFoundry instance of the service
var apiUrl = 'http://html5expense-api.cloudfoundry.com/';

// Use the following URL to test against a local instance of the service while running on the Android emulator
// var apiUrl = 'http://10.0.2.2:8080/api/';

// Use the following URL to test against a local instance of the service while running on the iPhone simulator
// var apiUrl = 'http://127.0.0.1:8080/api/';

function getApiUrl(path) {
    return apiUrl + path;
}


// ***************************************
// Initialization
// ***************************************

// In order to use the camera hardware, we need to hook into the device
document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {
    pictureSource = navigator.camera.PictureSourceType;
    destinationType = navigator.camera.DestinationType;

    // TODO: also check the offline/online events for enabling

    var networkState = navigator.network.connection.type;

    var states = {};
    states[Connection.UNKNOWN] = 'Unknown connection';
    states[Connection.ETHERNET] = 'Ethernet connection';
    states[Connection.WIFI] = 'WiFi connection';
    states[Connection.CELL_2G] = 'Cell 2G connection';
    states[Connection.CELL_3G] = 'Cell 3G connection';
    states[Connection.CELL_4G] = 'Cell 4G connection';
    states[Connection.NONE] = 'No network connection';

    console.log('Connection type: ' + states[networkState]);
}


// ***************************************
// Home
// ***************************************

$('#home').live('pageshow', function(event) {
    var content = '';
    if (isAuthorized()) {
        content += '<li><a href="#create-new-purpose">Create New</a></li>';
        content += '<li><a href="#expense-reports-open">Open Expense Reports</a></li>';
        content += '<li><a href="#sign-out">Sign Out</a></li>';
        content += '<li><a href="#about">About</a></li>';
    } else {
        content += '<li><a href="#sign-in">Sign In</a></li>';
        content += '<li><a href="#about">About</a></li>';
    }
    $('#home-menu-items').html(content).listview('refresh');
});


// ***************************************
// Sign In
// ***************************************

$('#sign-in').live('pagecreate', function(event) {
    $("#sign-in-form").validate({
        submitHandler : function(form) {
            console.log('Sign In');
            authorize();
        }
    });
});

function authorize() {
    $.mobile.showPageLoadingMsg();

//    var url = getApiUrl('oauth/token');
    var url = 'http://haboauth.cloudfoundry.com/oauth/token';
    var postData = {
         grant_type : 'password',
         username : $('#username').val(),
         password : $('#password').val(),
         client_id : clientId,
         client_secret : clientSecret,
         scope : 'read'
    };
    
    console.log(postData);
    
    $.ajax({
        type : 'POST',
        url : url,
        cache : false,
        dataType : 'json',
        data : postData,
        success : onAuthorizeSuccess,
        error : onAuthorizeError
    });
}

function onAuthorizeSuccess(data, textStatus, jqXHR) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Data: ' + JSON.stringify(data))

    if (data != null) {
        console.log(data.access_token);
        setAccessToken(data);
    }
 
    $.mobile.changePage('#home');
}

function onAuthorizeError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error signing in');
}


// ***************************************
// Sign Out
// ***************************************

$('#sign-out').live('pagecreate', function(event) {
    $('#sign-out-confirm').click(function() {
        removeAccessToken();
        $.mobile.changePage('#home');
    });
});


// ***************************************
// Create New - Purpose
// ***************************************

$('#create-new-purpose').live('pagecreate', function(event) {
    $("#create-new-purpose-form").validate({
        submitHandler : function(form) {
            console.log('Create New Purpose Submitted');
            submitCreateNewReportForm();
        }
    });
});

$('#create-new-purpose').live('pagebeforeshow', function(event) {
    $('#purpose').val('');
});

function submitCreateNewReportForm() {
    $.mobile.showPageLoadingMsg();

    var url = getApiUrl('reports');
    var formData = $('#create-new-purpose-form').serialize();

    console.log('URL: ' + url);
    console.log('Data: ' + formData);

    $.ajax({
        type : 'POST',
        url : url,
        cache : false,
        data : formData,
        success : onCreateReportSuccess,
        error : onCreateReportError
    });
}

function onCreateReportSuccess(data, textStatus, jqXHR) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    expenseReport = {};
    expenseReport.id = $.trim(data);
    $.mobile.changePage($('#create-new-expenses'));
}

function onCreateReportError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error creating report');
}


// ***************************************
// Create New - Expenses
// ***************************************

$('#create-new-expenses').live('pagecreate', function(event) {
    $('#create-new-expenses-next').click(function() {
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

$('#create-new-expenses').live('pagebeforeshow', function(event, ui) {
    $.mobile.showPageLoadingMsg();

    var url = getApiUrl('reports/eligible-charges');

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
    expenseReport.flaggedExpenses = getFlaggedExpenses(data);

    // if (expenseReport.flaggedExpenses && expenseReport.flaggedExpenses.length > 0) {
    if (receiptsRequired(expenseReport.expenses)) {
        $.mobile.changePage($('#create-new-add-receipt'));
    } else {
        // if no receipts are required, then display the confirmation page
        $.mobile.changePage($('#create-new-confirm'));
    }
}

function onAssociateExpensesError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error associating expenses');
}

function onFetchEligibleExpensesSuccess(data, textStatus, jqXHR) {
    if (data.length == 0) {
        $.mobile.hidePageLoadingMsg();
        $('#create-new-expenses-next').button('disable');
        alert('There are no available expenses!');
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
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error fetching eligible expenses');
}


// ***************************************
// Create New - Add Receipt
// ***************************************

$('#create-new-add-receipt').live('pagecreate', function(event) {

    $('#create-new-add-receipt-capture-photo').click(function() {
        navigator.camera.getPicture(onPhotoCaptureSuccess, onPhotoCaptureError, {
            quality : imageQuality,
            targetWidth : imageWidth,
            destinationType : destinationType.FILE_URI
        });
        return false;
    });

    $('#create-new-add-receipt-skip').click(function() {
        alert('Receipts are required in order to submit this expense report!');
        // TODO: determine if another receipt is required
        $.mobile.changePage($('#create-new-confirm'));
        return false;
    });
});

$('#create-new-add-receipt').live('pagebeforeshow', function(event) {
    updateExpenseDetails();
});

var expense;
function updateExpenseDetails() {
    expense = expenseReport.flaggedExpenses.pop();
    var content = '<h3>' + expense.merchant + '</h3>';
    content += '<p>$' + $.currency(expense.amount) + '</p>';
    content += '<p>' + expense.category + '</p>';
    content += '<p>' + formatDate(expense.date) + '</p>';
    $('#expense-details').html(content);
}

function onPhotoCaptureSuccess(imageURI) {
    $.mobile.showPageLoadingMsg();

    var image = document.getElementById('receiptImage');
    image.src = imageURI;

    /*
     * the FileTransfer function returns a FileUploadResult object upon success
     * http://docs.phonegap.com/en/1.0.0/phonegap_file_file.md.html#FileUploadResult
     */
    function onFileTransferSuccess(result) {
        $.mobile.hidePageLoadingMsg();
        console.log('Bytes Sent: ' + result.byteSent);
        console.log('Response Code: ' + result.responseCode);
        console.log('Response: ' + result.response);
        alert('Image uploaded successfully!');

        if (expenseReport.flaggedExpenses.length > 0) {
            updateExpenseDetails();
        } else {
            $.mobile.changePage($('#create-new-confirm'));
        }
    }

    /*
     * the FileTransfer function returns a FileTransferError object upon failure
     * http://docs.phonegap.com/en/1.0.0/phonegap_file_file.md.html#FileTransferError
     */
    function onFileTransferError(error) {
        $.mobile.hidePageLoadingMsg();
        console.log('Error Code: ' + error.code);
        alert('Image failed to upload! Please try again.');
    }

    /*
     * use a FileUploadOptions object to upload the image data
     * http://docs.phonegap.com/en/1.0.0/phonegap_file_file.md.html#FileUploadOptions
     */
    var opts = new FileUploadOptions();
    opts.fileKey = 'receiptBytes';
    opts.fileName = imageURI.substr(imageURI.lastIndexOf('/') + 1);
    opts.mimeType = 'image/jpeg';
    opts.params = {};

    /*
     * the FileTransfer function performs the AJAX request
     * http://docs.phonegap.com/en/1.0.0/phonegap_file_file.md.html#FileTransfer
     */
    var url = getApiUrl('reports/' + expenseReport.id + '/expenses/' + expense.id + '/receipt');
    var fileTransfer = new FileTransfer();
    fileTransfer.upload(imageURI, url, onFileTransferSuccess, onFileTransferError, opts);
}

function onPhotoCaptureError(message) {
    alert('Failed to capture image: ' + message);
}


// ***************************************
// Create New - Confirm
// ***************************************

$('#create-new-confirm').live('pagecreate', function(event) {
    $('#create-new-confirm-submit').click(function() {
        $.mobile.showPageLoadingMsg();

        var url = getApiUrl('reports/' + expenseReport.id);

        console.log('URL: ' + url);

        $.ajax({
            type : 'GET',
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
    console.log('Status: ' + textStatus);
    if (data == true) {
        alert('Expense report submitted');
        $.mobile.changePage($('#home'));
    } else {
        alert('Receipts required! Expense report not submitted');
    }
}

function onSubmitExpenseReportError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error submitting expense report');
}


// ***************************************
// Open Expense Reports
// ***************************************

$('#expense-reports-open').live('pageshow', function(event, ui) {
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
        // var expenseReportId = $(this).jqmData('id');
        // $('#review-status-details').jqmData('expenseReportId', expenseReportId);
        // expenseReport = getOpenExpenseReport(expenseReportId);
        // $.mobile.changePage('#create-new-confirm');
    });
});

var openExpenseReports;
function getOpenExpenseReport(id) {
    $.each(openExpenseReports, function(i, r) {
        if (r.id == id) {
            return r;
        }
    });
    return null;
}

function onFetchOpenExpenseReportsSuccess(data, textStatus, jqXHR) {
    openExpenseReports = data;
    console.log('Status: ' + textStatus);
    if (openExpenseReports.length == 0) {
        $.mobile.hidePageLoadingMsg();
        $('#expense-reports-open-list').html('').listview('refresh');
        alert('There are no open expense reports');
    } else {
        var content = '';
        $.each(openExpenseReports, function(i, expenseReport) {
            if (expenseReport.purpose != null) {
                content += '<li data-id=' + expenseReport.id + ' class="expense-report-list-item"><a href="#">';
                content += '<p class="ui-li-count">' + expenseReport.expenses.length + '</p>';
                content += '<h3>' + expenseReport.purpose + '</h3>';
                content += '<p>Status: ' + expenseReport.state + '</p>';
                content += '</a></li>';
            }

            // set the new content and refresh the UI
            $('#expense-reports-open-list').html(content).listview('refresh');

            $.mobile.hidePageLoadingMsg();
        });
    }
}

function onFetchOpenExpenseReportsError(jqXHR, textStatus, errorThrown) {
    $.mobile.hidePageLoadingMsg();
    console.log('Status: ' + textStatus);
    console.log('Error: ' + errorThrown);
    alert('Error fetching open expense reports');
}


// ***************************************
// Review Status Details
// ***************************************

$('#review-status-details').live('pagebeforeshow', function(event, ui) {
    var expenseReportId = $('#review-status-details').jqmData('expenseReportId');
    if (expenseReportId != null) {
        $('#review-status-details-text').text('Details about expense report #' + expenseReportId + ' here.');
    } else {
        alert('No expense report available to display');
    }
});


// ***************************************
// Utility Methods
// ***************************************

function formatDate(date) {
    var dateString = JSON.stringify(date);
    // TODO: is there a better way to do this?
    return dateString.substring(1, dateString.length - 1).replace(/,/g, "-");
}

function receiptsRequired(expenses) {
    var result = false;
    $.each(expenses, function(i, expense) {
        if (expense.flag == 'receiptRequired') {
            result = true;
        }
    });
    return result;
}

function getFlaggedExpenses(expenses) {
    var a = [];
    $.each(expenses, function(i, expense) {
        if (expense.flag == 'receiptRequired') {
            a.push(expense);
        }
    });
    return a.reverse();
}

// determines if the user has a valid access token
function isAuthorized() {
    if (getAccessToken() != null) {
        return true;
    }
    return false;
}

var ACCESS_TOKEN = 'AccessToken';

// saves the access token to local storage
function setAccessToken(accessToken) {
    if (accessToken != null) {
        localStorage.setItem(ACCESS_TOKEN, JSON.stringify(accessToken));
    }
}

// retrieves the access token from local storage
function getAccessToken() {
    var t = localStorage.getItem(ACCESS_TOKEN);
    return t && JSON.parse(t);
}

// remove the access token (deauthorize)
function removeAccessToken() {
    localStorage.removeItem(ACCESS_TOKEN);
}
