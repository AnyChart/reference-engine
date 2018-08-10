const selectedVersion = () => $("#versionSelect :selected").text();


const errorFn = (jqXHR, textStatus, errorThrown) => {
    alert('An error occurred... Look at the console for more information!')
};


const rebuildSuccessFn = (data, textStatus, jqXHR) => {
    alert("Rebuild has started!");
    window.location.href = "/_admin_";
};


window.onload = (e) => {
    $('#deleteButton').click((e) => {
        $('button').prop('disabled', true);
        $.ajax({
            type: "POST",
            url: `/_delete_`,
            data: {version: selectedVersion()},
            success: (data, textStatus, jqXHR) => {
                window.location.href = "/_admin_";
            },
            error: errorFn
        });
    });

    $('#rebuildCommit').click((e) => {
        $('button').prop('disabled', true);
        e.preventDefault();
        $.ajax({
            type: "POST", url: `/_rebuild_`,
            data: {version: selectedVersion()},
            success: rebuildSuccessFn, error: errorFn
        });
    });

    $('#rebuildFast').click((e) => {
        $('button').prop('disabled', true);
        e.preventDefault();
        $.ajax({
            type: "POST", url: `/_rebuild_`,
            data: {
                version: selectedVersion(),
                fast: true
            },
            success: rebuildSuccessFn, error: errorFn
        });
    });

    $('#rebuildFull').click((e) => {
        $('button').prop('disabled', true);
        e.preventDefault();
        $.ajax({
            type: "POST", url: `/_rebuild_`,
            data: {
                version: selectedVersion(),
                dts: true
            },
            success: rebuildSuccessFn, error: errorFn
        });
    });

    $('#indexLink').click((e) => {
        const index = $("#versionSelect").prop('selectedIndex');
        if (index == 0) {
            window.location.href = `/si/${selectedVersion()}/index.d.ts`
        } else {
            window.location.href = `/si/${selectedVersion()}/index-${selectedVersion()}.d.ts`
        }
    });

    $('#graphicsLink').click((e) => {
        const index = $("#versionSelect").prop('selectedIndex');
        if (index == 0) {
            window.location.href = `/si/${selectedVersion()}/graphics.d.ts`
        } else {
            window.location.href = `/si/${selectedVersion()}/graphics-${selectedVersion()}.d.ts`
        }
    });
};