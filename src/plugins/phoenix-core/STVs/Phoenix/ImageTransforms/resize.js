function transform(image, args) {
	var awt = new JavaImporter(java.awt.image);
    var StaticImage = Java.type("java.awt.image.BufferedImage");
	with (awt) {
        var img = image.getScaledInstance(args.getInt('width'), args.getInt('height'), StaticImage.SCALE_SMOOTH);
        var bi = new BufferedImage(args.getInt('width'), args.getInt('height'), StaticImage.TYPE_INT_ARGB);
        var biContext = bi.createGraphics();
        biContext.drawImage(img, 0, 0, null);
        return bi;
    }
}
