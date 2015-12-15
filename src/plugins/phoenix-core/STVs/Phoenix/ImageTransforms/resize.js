function transform(image, args) {
	var awt = new JavaImporter(java.awt.image);
	with (awt) {
        var img = image.getScaledInstance(args.getInt('width'), args.getInt('height'), image.SCALE_SMOOTH);
        var bi = new BufferedImage(args.getInt('width'), args.getInt('height'), image.TYPE_INT_RGB);
        var biContext = bi.createGraphics();
        biContext.drawImage(img, 0, 0, null);
        return bi;
    }
}
