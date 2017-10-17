concatUint8ByteArrays = function (firstArray, secondArray) {
    let joinedArray = new Uint8Array(firstArray.length + secondArray.length);
    joinedArray.set(firstArray);
    joinedArray.set(secondArray, firstArray.length);
    return joinedArray;
}


function ipLongToString(num)
{
    let d = num%256;
    for (let i = 3; i > 0; i--)
    {
        num = Math.floor(num/256);
        d = num%256 + '.' + d;
    }
    return d;
}

function modulo(a, b) {
    return a - Math.floor(a/b)*b;
}

function ToUint32(x) {
    return modulo(ToInteger(x), Math.pow(2, 32));
}

function ToInteger(x) {
    x = Number(x);
    return x < 0 ? Math.ceil(x) : Math.floor(x);
}

/**
 * Puts an int into its bytes and put them into a byte array
 * @param array The array to be put into
 * @param start The starting index to put number into
 * @param numBytes The number of bytes of the int
 * @param item The actual value of the int
 */
addIntIntoByteArray = function (array, start, numBytes, item) {
    for (let i = 0; i < numBytes; i++) {
        array[start + i] = (item >> (i * 8)) & (0xFF);
    }
};


byteArrayRangeToInt = function (array, beginIndex, length) {
    let total = 0;
    if (length <= 0 || length > 4){
        console.error("The length of the range must be between 1 and 4 inclusive");
    }
    for (let i = (beginIndex + length) - 1; i >= beginIndex; i--){
        total = (total << 8) + (array[i] & 0xFF);
    }
    return total;
}


function componentToHex(c) {
    var hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
}

function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
}

function charArrayToString(array){
    let string = "";
    for(let i = 0; i < array.length; i++){
        if(array[i] === 0) break;
        string += String.fromCharCode(array[i]);
    }
    return string;
}
